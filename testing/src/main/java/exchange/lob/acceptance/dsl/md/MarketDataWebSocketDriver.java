package exchange.lob.acceptance.dsl.md;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import exchange.lob.api.sbe.Side;
import exchange.lob.md.dto.OrderCanceledUpdate;
import exchange.lob.md.dto.OrderPlacedUpdate;
import exchange.lob.md.dto.TradeUpdate;
import org.agrona.collections.MutableLong;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import static exchange.lob.Assertions.assertReflectiveContains;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarketDataWebSocketDriver extends WebSocketClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataWebSocketDriver.class);

    public static final long TIMESTAMP = 0L;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Set<OrderPlacedUpdate> placeOrderUpdates = Sets.newConcurrentHashSet();
    private final MutableLong placeOrderUpdatesToMatch = new MutableLong(0L);

    private final Set<OrderCanceledUpdate> cancelOrderUpdates = Sets.newConcurrentHashSet();
    private final MutableLong cancelOrderUpdatesToMatch = new MutableLong(0L);

    private final Set<TradeUpdate> tradeUpdates = Sets.newConcurrentHashSet();
    private final MutableLong tradeUpdatesToMatch = new MutableLong(0L);

    private final AtomicBoolean webSocketOpened = new AtomicBoolean(false);

    public MarketDataWebSocketDriver(URI serverURI)
    {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake)
    {
        LOGGER.info("MarketDataWebSocketDriver onOpen()");
        webSocketOpened.set(true);
    }

    public void waitForWebsocketToOpen()
    {
        await()
            .timeout(5, TimeUnit.SECONDS)
            .untilTrue(webSocketOpened);
    }

    @Override
    public void onMessage(String message)
    {
        try
        {
            LOGGER.info("Received message {}", message);
            final String messageType = objectMapper.readTree(message).path("type").asText();

            switch (messageType)
            {
                case "place" ->
                {
                    final OrderPlacedUpdate orderPlacedUpdate = objectMapper.readValue(message, OrderPlacedUpdate.class);
                    placeOrderUpdates.add(orderPlacedUpdate);
                    placeOrderUpdatesToMatch.increment();
                }
                case "cancel" ->
                {
                    final OrderCanceledUpdate orderCanceledUpdate = objectMapper.readValue(message, OrderCanceledUpdate.class);
                    cancelOrderUpdates.add(orderCanceledUpdate);
                    cancelOrderUpdatesToMatch.increment();
                }
                case "trade" ->
                {
                    final TradeUpdate tradeUpdate = objectMapper.readValue(message, TradeUpdate.class);
                    tradeUpdates.add(tradeUpdate);
                    tradeUpdatesToMatch.increment();
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {

    }

    @Override
    public void onError(Exception ex)
    {
        LOGGER.error("Error: ", ex);
    }

    public void verifyPlaceOrderUpdateReceived(final String symbol, final Side side, final double price, final double amount)
    {
        final OrderPlacedUpdate orderPlacedUpdate = new OrderPlacedUpdate(TIMESTAMP, symbol, side.name(), price, amount);

        await().timeout(20, TimeUnit.SECONDS)
            .pollDelay(1, TimeUnit.SECONDS)
            .until(() -> {
                LOGGER.info("Update: {}, Current collection: {}", orderPlacedUpdate, placeOrderUpdates);
                assertReflectiveContains(orderPlacedUpdate, placeOrderUpdates, "timestamp");
                return true;
            });

        placeOrderUpdatesToMatch.decrement();
    }

    public void verifyCancelOrderUpdateReceived(final String symbol, final Side side, final double price, final double amount)
    {
        final OrderCanceledUpdate orderCanceledUpdate = new OrderCanceledUpdate(TIMESTAMP, symbol, side.name(), price, amount);

        await().timeout(20, TimeUnit.SECONDS)
            .pollDelay(1, TimeUnit.SECONDS)
            .until(() -> {
                LOGGER.info("Update: {}, Current collection: {}", orderCanceledUpdate, cancelOrderUpdates);
                assertReflectiveContains(orderCanceledUpdate, cancelOrderUpdates, "timestamp");
                return true;
            });

        cancelOrderUpdatesToMatch.decrement();
    }

    public void verifyTradeUpdateReceived(final String symbol, final Side takerSide, final double price, final double amount)
    {
        final TradeUpdate tradeUpdate = new TradeUpdate(TIMESTAMP, symbol, takerSide.name(), price, amount);

        await().timeout(20, TimeUnit.SECONDS)
            .pollDelay(1, TimeUnit.SECONDS)
            .until(() -> {
                LOGGER.info("Update: {}, Current collection: {}", tradeUpdate, tradeUpdates);
                assertReflectiveContains(tradeUpdate, tradeUpdates, "timestamp");
                return true;
            });

        tradeUpdatesToMatch.decrement();
    }

    public void noMoreUpdates()
    {
        assertEquals(0L, placeOrderUpdatesToMatch.value, "Some place order updates are unmatched!");
        assertEquals(0L, cancelOrderUpdatesToMatch.value, "Some cancel order updates are unmatched!");
        assertEquals(0L, tradeUpdatesToMatch.value, "Some trade updates are unmatched!");
    }

    public static void main(String[] args)
    {
        final MarketDataWebSocketDriver marketDataWebSocketDriver = new MarketDataWebSocketDriver(URI.create("ws://localhost:8082"));
        marketDataWebSocketDriver.connect();
        marketDataWebSocketDriver.waitForWebsocketToOpen();
        LockSupport.parkNanos(TimeUnit.HOURS.toNanos(1));
    }
}