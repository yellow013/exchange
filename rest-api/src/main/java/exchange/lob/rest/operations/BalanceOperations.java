package exchange.lob.rest.operations;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.node.client.response.Balance;
import io.vertx.core.json.Json;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class BalanceOperations extends Operation
{

    private final AdminClient adminClient;

    public BalanceOperations(final AdminClient adminClient)
    {
        super("balances");
        this.adminClient = adminClient;
    }

    @Override
    protected void handle(final RoutingContext routingContext)
    {
        final User userContext = routingContext.user();
        final long userId = userContext.attributes().getLong("userId");
        final Map<String, Double> balancesResponse = adminClient.fetchUserBalances(userId).join().balances().stream()
            .collect(toMap(Balance::getAssetSymbol, Balance::getBalance));

        routingContext
            .response()
            .send(Json.encodePrettily(balancesResponse));
    }
}
