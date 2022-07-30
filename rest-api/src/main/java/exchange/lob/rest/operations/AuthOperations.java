package exchange.lob.rest.operations;

import io.vertx.ext.web.RoutingContext;

public class AuthOperations extends Operation
{
    public AuthOperations()
    {
        super("auth");
    }

    @Override
    protected void handle(final RoutingContext routingContext)
    {
        routingContext
            .response()
            .send("OK");
    }
}
