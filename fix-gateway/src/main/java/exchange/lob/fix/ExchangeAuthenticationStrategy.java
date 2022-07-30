package exchange.lob.fix;

import exchange.lob.api.codecs.fix.builder.LogoutEncoder;
import exchange.lob.events.admin.AdminClient;
import exchange.lob.node.client.response.VerifyUserResponse;
import exchange.lob.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.artio.decoder.AbstractLogonDecoder;
import uk.co.real_logic.artio.validation.AuthenticationProxy;
import uk.co.real_logic.artio.validation.AuthenticationStrategy;

import java.util.Map;

public class ExchangeAuthenticationStrategy implements AuthenticationStrategy
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeAuthenticationStrategy.class);

    public static final LogoutEncoder LOGOUT_ENCODER = new LogoutEncoder();
    private final AdminClient adminClient;
    private final Map<String, User> users;

    public ExchangeAuthenticationStrategy(final AdminClient adminClient, final Map<String, User> users)
    {
        this.adminClient = adminClient;
        this.users = users;
    }

    @Override
    public void authenticateAsync(final AbstractLogonDecoder logon, final AuthenticationProxy authProxy)
    {
        if (authenticate(logon))
        {
            authProxy.accept();
        }
        else
        {
            authProxy.reject(LOGOUT_ENCODER, 1000L);
        }
    }

    @Override
    public boolean authenticate(final AbstractLogonDecoder logon)
    {
        final String username = logon.usernameAsString();
        final String password = logon.passwordAsString();

        LOGGER.info("Authenticating: {}", username);

        final VerifyUserResponse response = adminClient.verifyUser(username, password).join();

        final boolean verified = response.verified();

        if (verified)
        {
            users.put(username, new User(response.userId(), username, password));
        }

        LOGGER.info("{} authenticated: {}", username, verified);

        return verified;
    }
}
