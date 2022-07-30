package exchange.lob.node.client;

public enum ExchangeClusterPrincipal
{

    ADMIN_API("admin-api"),
    FIX_GATEWAY("fix-gateway"),
    WS_SERVER("ws-server"),
    REST_API("rest-api");

    public final String name;

    ExchangeClusterPrincipal(final String name)
    {
        this.name = name;
    }
}
