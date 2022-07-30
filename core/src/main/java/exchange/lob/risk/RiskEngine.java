package exchange.lob.risk;

import exchange.lob.api.codecs.internal.ExchangeStateDecoder;
import exchange.lob.api.codecs.internal.ExchangeStateEncoder;
import exchange.lob.api.codecs.internal.OrderType;
import exchange.lob.api.codecs.internal.Side;
import exchange.lob.domain.ExchangeResponseCode;
import exchange.lob.domain.RejectionReason;
import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.match.execution.ExecutionReport;
import exchange.lob.node.Stateful;
import exchange.lob.node.client.response.UpdateBalanceResponse;
import exchange.lob.product.Asset;
import exchange.lob.product.Product;
import org.agrona.collections.Long2LongHashMap;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.collections.LongHashSet;

import java.util.Objects;
import java.util.function.LongSupplier;

import static exchange.lob.user.UserService.EXCHANGE_USER_ID;

public class RiskEngine
{

    private static final long MISSING_VALUE = -1L;

    private final OrderBookEvents orderBookEvents;

    /**
     * User ID -> Asset ID -> Balance
     */
    Long2ObjectHashMap<Long2LongHashMap> balances;
    LongHashSet assetIds;

    public static final Codec CODEC = new Codec();

    RiskEngine(
        final OrderBookEvents orderBookEvents,
        final Long2ObjectHashMap<Long2LongHashMap> balances,
        final LongHashSet assetIds
    )
    {
        this.orderBookEvents = orderBookEvents;
        this.balances = balances;
        this.assetIds = assetIds;
    }


    public static RiskEngine withOrderBookEvents(final OrderBookEvents orderBookEvents)
    {
        final Long2ObjectHashMap<Long2LongHashMap> balances = new Long2ObjectHashMap<>();
        balances.put(EXCHANGE_USER_ID, new Long2LongHashMap(MISSING_VALUE));
        return RiskEngine.builder()
            .orderBookEvents(orderBookEvents)
            .balances(balances)
            .assetIds(new LongHashSet())
            .build();
    }

    public static RiskEngineBuilder builder()
    {
        return new RiskEngineBuilder();
    }

    public void addUser(final long userId)
    {
        Long2LongHashMap newUserBalances = new Long2LongHashMap(MISSING_VALUE);
        assetIds.forEach(assetId -> newUserBalances.put(assetId, 0));
        balances.put(userId, newUserBalances);
    }

    public void addAsset(final long assetId)
    {
        assetIds.add(assetId);
        balances.forEach((userId, balances) -> balances.put(assetId, 0L));
    }

    public UpdateBalanceResponse updateBalance(final long userId, final long assetId, final long amount)
    {
        final Long2LongHashMap userBalances = balances.get(userId);
        final long assetBalance = userBalances.get(assetId);
        final boolean isNewBalance = assetBalance == MISSING_VALUE;
        if (amount < 0 && -amount > assetBalance && !isNewBalance)
        {
            return new UpdateBalanceResponse(ExchangeResponseCode.BALANCE_OVERDRAWN);
        }
        if (amount < 0 && isNewBalance)
        {
            return new UpdateBalanceResponse(ExchangeResponseCode.BALANCE_OVERDRAWN);
        }
        if (isNewBalance && amount > 0)
        {
            // first deposit of the given asset by the given user
            userBalances.put(assetId, amount);
            return new UpdateBalanceResponse(ExchangeResponseCode.SUCCESS);
        }

        // top-up
        userBalances.put(assetId, assetBalance + amount);
        return new UpdateBalanceResponse(ExchangeResponseCode.SUCCESS);
    }

    public Long2LongHashMap getBalances(final long userId)
    {
        return balances.get(userId);
    }

    public long reserveBalance(
        final long correlationId,
        final String clientOrderId,
        final long userId,
        final Product product,
        final OrderType orderType,
        final Side side,
        final long price,
        final long amount,
        final LongSupplier executionIdSupplier
    )
    {
        final Asset asset = side == Side.BID ? product.getCounterAsset() : product.getBaseAsset();
        final Long2LongHashMap userBalances = balances.get(userId);

        final long assetBalance = userBalances.get(asset.getAssetId());
        final long requiredBalance = calculateRequiredBalance(product, orderType, side, price, amount, assetBalance);
        if (requiredBalance > assetBalance)
        {
            orderBookEvents.onOrderRejected(
                correlationId,
                clientOrderId,
                executionIdSupplier.getAsLong(),
                product.getSymbol(),
                userId,
                exchange.lob.domain.Side.fromSbe(side),
                RejectionReason.INSUFFICIENT_BALANCE
            );
            return Long.MIN_VALUE;
        }

        userBalances.put(asset.getAssetId(), assetBalance - requiredBalance);
        return requiredBalance;
    }

    public void settleExecutionReports(final ExecutionReport executionReport)
    {
        executionReport.settle(balances);
    }

    /**
     * If {@param orderType} is {@link OrderType#MKT} and {@param side} is {@link Side#BID}:
     * the engine reserves the entire counterAsset balance of the user.
     * <p>
     * If {@param orderType} is {@link OrderType#MKT} and {@param side} is {@link Side#ASK}:
     * the engine reserves the {@param amount} - takerFee of baseAsset balance of the user.
     * <p>
     * If {@param orderType} is {@link OrderType#LMT} and {@param side} is {@link Side#BID}:
     * the engine reserves the {@param amount} * {@param price} of counterAsset balance of the user.
     * <p>
     * If {@param orderType} is {@link OrderType#LMT} and {@param side} is {@link Side#ASK}:
     * the engine reserves the {@param amount} of baseAsset balance of the user.
     *
     * @param product      product traded
     * @param orderType    order type
     * @param side         order side
     * @param price        price in case of {@link OrderType#LMT} order
     * @param amount       order amount
     * @param assetBalance available balance
     * @return balance to reserve
     */
    private long calculateRequiredBalance(
        final Product product,
        final OrderType orderType,
        final Side side,
        final long price,
        final long amount,
        final long assetBalance
    )
    {
        return switch (orderType)
            {
                case LMT -> switch (side)
                    {
                        case BID -> product.deal(price, amount);
                        case ASK -> amount;
                        case NULL_VAL -> Long.MAX_VALUE;
                    };
                case MKT -> switch (side)
                    {
                        case BID -> assetBalance;
                        case ASK -> amount;
                        case NULL_VAL -> Long.MAX_VALUE;
                    };
                case NULL_VAL -> Long.MAX_VALUE;
            };
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final RiskEngine that = (RiskEngine)o;
        return Objects.equals(balances, that.balances) && Objects.equals(assetIds, that.assetIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(balances, assetIds);
    }

    @Override
    public String toString()
    {
        return "RiskEngine{" +
            "orderBookEvents=" + orderBookEvents +
            ", balances=" + balances +
            ", assetIds=" + assetIds +
            '}';
    }

    public static class Codec implements Stateful<RiskEngine>
    {

        @Override
        public RiskEngine decodeState(final ExchangeStateDecoder exchangeStateDecoder)
        {
            final Long2ObjectHashMap<Long2LongHashMap> balances = new Long2ObjectHashMap<>();
            final LongHashSet assetIds = new LongHashSet();
            balances.put(EXCHANGE_USER_ID, new Long2LongHashMap(MISSING_VALUE));
            exchangeStateDecoder.balances().forEach(balance -> {
                long userId = balance.userId();
                assetIds.add(balance.assetId());
                Long2LongHashMap userBalances = balances.get(userId);
                if (userBalances == null)
                {
                    Long2LongHashMap newUserBalances = new Long2LongHashMap(MISSING_VALUE);
                    newUserBalances.put(balance.assetId(), balance.balance());
                    balances.put(userId, newUserBalances);
                }
                else
                {
                    userBalances.put(balance.assetId(), balance.balance());
                }
            });

            return RiskEngine.builder()
                .balances(balances)
                .assetIds(assetIds)
                .build();
        }

        @Override
        public void encodeState(final RiskEngine riskEngine, final ExchangeStateEncoder exchangeStateEncoder)
        {
            final Long2ObjectHashMap<Long2LongHashMap> balances = riskEngine.balances;
            final ExchangeStateEncoder.BalancesEncoder balancesEncoder = exchangeStateEncoder
                .balancesCount(balances.size() * riskEngine.assetIds.size());

            balances.forEach((userId, userBalances) -> userBalances
                .forEach((assetId, balance) -> balancesEncoder.next().userId(userId).assetId(assetId).balance(balance)));
        }
    }

    public static class RiskEngineBuilder
    {
        private OrderBookEvents orderBookEvents;
        private Long2ObjectHashMap<Long2LongHashMap> balances;
        private LongHashSet assetIds;

        RiskEngineBuilder()
        {
        }

        public RiskEngineBuilder orderBookEvents(OrderBookEvents orderBookEvents)
        {
            this.orderBookEvents = orderBookEvents;
            return this;
        }

        public RiskEngineBuilder balances(Long2ObjectHashMap<Long2LongHashMap> balances)
        {
            this.balances = balances;
            return this;
        }

        public RiskEngineBuilder assetIds(LongHashSet assetIds)
        {
            this.assetIds = assetIds;
            return this;
        }

        public RiskEngine build()
        {
            return new RiskEngine(orderBookEvents, balances, assetIds);
        }

        @Override
        public String toString()
        {
            return "RiskEngineBuilder{" +
                "orderBookEvents=" + orderBookEvents +
                ", balances=" + balances +
                ", assetIds=" + assetIds +
                '}';
        }
    }
}
