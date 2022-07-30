package exchange.lob.rest.operations;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;

public abstract class Operation
{
    private final String operationName;

    public Operation(final String operationName)
    {
        this.operationName = operationName;
    }

    public void wire(final RouterBuilder routerBuilder)
    {
        routerBuilder.operation(operationName).handler(routingContext -> {
            attachCorsHeaders(routingContext);
            handle(routingContext);
        });
    }

    private void attachCorsHeaders(final RoutingContext routingContext)
    {
        routingContext.response().putHeader("Access-Control-Allow-Origin", "*");
    }

    protected abstract void handle(final RoutingContext routingContext);
}
