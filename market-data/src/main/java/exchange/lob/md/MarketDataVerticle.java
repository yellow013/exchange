package exchange.lob.md;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.lob.md.dto.OrderCanceledUpdate;
import exchange.lob.md.dto.OrderPlacedUpdate;
import exchange.lob.md.dto.TradeUpdate;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ConcurrentHashSet;
import lob.exchange.config.marketdata.MarketDataWebsocketServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

class MarketDataVerticle extends AbstractVerticle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataVerticle.class);

    private final MarketDataWebsocketServerConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<ServerWebSocket> websocketSessions = new ConcurrentHashSet<>();

    public MarketDataVerticle(final MarketDataWebsocketServerConfig config)
    {
        this.config = config;
    }

    @Override
    public void start()
    {
        LOGGER.info("Starting MarketDataVerticle...");

        final HttpServer server = vertx.createHttpServer();

        server
            .webSocketHandler((websocket) -> {
                LOGGER.info("Session connected: {}", websocket);
                websocketSessions.add(websocket);
                websocket.closeHandler(event -> {
                    LOGGER.info("Session closed: {}", websocket);
                    websocketSessions.remove(websocket);
                });
            }).listen(config.getPort());
    }

    public void broadcast(final String data)
    {
        websocketSessions.forEach(session -> session.writeTextMessage(data));
    }

    public void processNewOrderUpdate(
        final long timestamp,
        final String productSymbol,
        final String side,
        final double price,
        final double amount
    )
    {
        final OrderPlacedUpdate marketUpdate = new OrderPlacedUpdate(timestamp, productSymbol, side, price, amount);
        broadcast(writeValue(marketUpdate));
    }

    public void processTradeUpdate(
        final long timestamp,
        final String productSymbol,
        final String side,
        final double price,
        final double amount
    )
    {
        final TradeUpdate marketUpdate = new TradeUpdate(timestamp, productSymbol, side, price, amount);
        broadcast(writeValue(marketUpdate));
    }

    public void processCancelOrderResponse(
        final long timestamp,
        final String productSymbol,
        final String side,
        final double price,
        final double amount
    )
    {
        final OrderCanceledUpdate marketUpdate = new OrderCanceledUpdate(timestamp, productSymbol, side, price, amount);
        broadcast(writeValue(marketUpdate));
    }

    private String writeValue(final Object obj)
    {
        try
        {
            return objectMapper.writeValueAsString(obj);
        }
        catch (final JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)
    {
        final MarketDataWebsocketServerConfig config = MarketDataWebsocketServerConfig.readConfig();
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MarketDataVerticle(config));
    }
}
