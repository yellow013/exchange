package exchange.lob.match;

import exchange.lob.api.codecs.internal.Side;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.Objects;

public class LiquidityCache
{
    private long availableBidLiquidity;
    private long availableAskLiquidity;

    public LiquidityCache(final long availableBidLiquidity, final long availableAskLiquidity)
    {
        this.availableBidLiquidity = availableBidLiquidity;
        this.availableAskLiquidity = availableAskLiquidity;
    }

    public static LiquidityCache create()
    {
        return new LiquidityCache(0L, 0L);
    }

    public static LiquidityCache fromBidsAndAsks(ObjectRBTreeSet<Order> bids, ObjectRBTreeSet<Order> asks)
    {
        long bidLiquidity = bids.stream().mapToLong(Order::getAmount).sum();
        long askLiquidity = asks.stream().mapToLong(Order::getAmount).sum();
        return new LiquidityCache(bidLiquidity, askLiquidity);
    }

    public void onLiquidityIncrease(Side side, long amount)
    {
        switch (side)
        {
            case BID -> availableBidLiquidity += amount;
            case ASK -> availableAskLiquidity += amount;
        }
    }

    public void onLiquidityDecrease(Side side, long amount)
    {
        switch (side)
        {
            case BID -> availableBidLiquidity -= amount;
            case ASK -> availableAskLiquidity -= amount;
        }
    }

    public long getAvailableLiquidity(Side side)
    {
        return side == Side.BID ? availableBidLiquidity : availableAskLiquidity;
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
        final LiquidityCache that = (LiquidityCache)o;
        return availableBidLiquidity == that.availableBidLiquidity && availableAskLiquidity == that.availableAskLiquidity;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(availableBidLiquidity, availableAskLiquidity);
    }
}
