package exchange.lob.rest.operations;

import io.vertx.ext.web.RoutingContext;

public class HealthCheckOperations extends Operation
{

    public HealthCheckOperations()
    {
        super("healthcheck");
    }

    @Override
    protected void handle(final RoutingContext routingContext)
    {
        routingContext
            .response()
            .send("OK");
    }
}
