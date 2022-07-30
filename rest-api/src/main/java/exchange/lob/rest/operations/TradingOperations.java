package exchange.lob.rest.operations;

import exchange.lob.domain.OrderType;
import exchange.lob.domain.Side;
import exchange.lob.events.trading.TradingRequests;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class TradingOperations extends Operation
{

    private final TradingRequests tradingRequests;

    public TradingOperations(final TradingRequests tradingRequests)
    {
        super("placeOrder");
        this.tradingRequests = tradingRequests;
    }

    @Override
    protected void handle(final RoutingContext routingContext)
    {
        final User userContext = routingContext.user();
        final String username = userContext.attributes().getString("username");

        final JsonObject body = routingContext.getBodyAsJson();
        final String product = body.getString("product");
        final String side = body.getString("side");
        final String price = body.getString("price");
        final String amount = body.getString("amount");

        tradingRequests.placeOrder(
            username,
            product,
            UUID.randomUUID().toString().substring(0, 20),
            OrderType.LMT,
            Side.valueOf(side),
            Double.parseDouble(price),
            Double.parseDouble(amount)
        );

        routingContext
            .response()
            .send("FILLED");
    }
}
