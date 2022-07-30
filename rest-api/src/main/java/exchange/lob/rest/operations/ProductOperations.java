package exchange.lob.rest.operations;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.product.Product;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductOperations extends Operation
{

    private final AdminClient adminClient;

    public ProductOperations(final AdminClient adminClient)
    {
        super("products");
        this.adminClient = adminClient;
    }

    @Override
    protected void handle(final RoutingContext routingContext)
    {
        final List<String> products = adminClient.fetchProducts().join().products().stream()
            .map(Product::getSymbol)
            .collect(Collectors.toList());

        routingContext
            .response()
            .send(Json.encodePrettily(Map.of("products", products)));
    }
}
