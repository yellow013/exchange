package exchange.lob.acceptance.dsl.fix;

import com.lmax.simpledsl.DslParams;
import com.lmax.simpledsl.OptionalParam;
import com.lmax.simpledsl.RequiredParam;
import exchange.lob.acceptance.TestStorage;

import java.util.HashMap;

public class FixClientDsl
{
    private final TestStorage testStorage;
    private final String gatewayHostname;
    private final HashMap<String, FixSessionDsl> clientSessionsByCompId = new HashMap<>();

    public FixClientDsl(final TestStorage testStorage, final String gatewayHostname)
    {
        this.testStorage = testStorage;
        this.gatewayHostname = gatewayHostname;
    }

    public void login(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("password"),
            new OptionalParam("authenticationFailure").setDefault("false")
        );

        final String username = params.value("username");
        final String password = params.value("password");
        final boolean authenticationFailure = params.valueAsBoolean("authenticationFailure");

        final FixSessionDsl clientSession = new FixSessionDsl(username, testStorage, gatewayHostname);
        clientSession.login(password, authenticationFailure);
        clientSessionsByCompId.put(username, clientSession);
    }

    public FixSessionDsl as(final String compId)
    {
        return clientSessionsByCompId.get(compId);
    }

    public void close()
    {
        closeAllSessions();
    }

    private void closeAllSessions()
    {
        clientSessionsByCompId.values().forEach(FixSessionDsl::close);
    }
}
