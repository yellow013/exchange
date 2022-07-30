package exchange.lob.node;

import io.aeron.security.Authenticator;
import io.aeron.security.SessionProxy;
import org.agrona.collections.Long2ObjectHashMap;

import java.nio.charset.StandardCharsets;


public class ExchangeAuthenticator implements Authenticator
{

    private final Long2ObjectHashMap<String> credentials = new Long2ObjectHashMap<>();

    public void onConnectRequest(final long sessionId, final byte[] encodedCredentials, final long nowMs)
    {
        final String credentialsString = new String(encodedCredentials, StandardCharsets.US_ASCII);
        credentials.put(sessionId, credentialsString);
    }

    public void onChallengeResponse(final long sessionId, final byte[] encodedCredentials, final long nowMs)
    {
    }

    public void onConnectedSession(final SessionProxy sessionProxy, final long nowMs)
    {
        final String credentialsToEcho = credentials.get(sessionProxy.sessionId());
        if (null != credentialsToEcho)
        {
            sessionProxy.authenticate(credentialsToEcho.getBytes());
        }
    }

    public void onChallengedSession(final SessionProxy sessionProxy, final long nowMs)
    {
        final String credentialsToEcho = credentials.get(sessionProxy.sessionId());
        if (null != credentialsToEcho)
        {
            sessionProxy.authenticate(credentialsToEcho.getBytes());
        }
    }
}
