package exchange.lob.rest.auth;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.node.client.response.VerifyUserResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.impl.UserImpl;

import java.util.HashMap;
import java.util.Map;

public class ExchangeUserAuthenticationProvider implements AuthenticationProvider
{
    private final Map<String, String> verifiedUsernameToPassword = new HashMap<>();
    private final Map<String, User> userContextToUserIdMap = new HashMap<>();
    private final AdminClient adminClient;

    public ExchangeUserAuthenticationProvider(final AdminClient adminClient)
    {
        this.adminClient = adminClient;
    }

    @Override
    public void authenticate(final JsonObject credentials, final Handler<AsyncResult<User>> resultHandler)
    {
        final String username = credentials.getString("username");
        final String providedPassword = credentials.getString("password");
        final String existingPassword = verifiedUsernameToPassword.get(username);

        if (existingPassword == null)
        {
            final VerifyUserResponse verifyUserResponse = adminClient.verifyUser(username, providedPassword).join();
            final boolean verified = verifyUserResponse.verified();

            if (verified)
            {
                final long userId = verifyUserResponse.userId();
                final User userContext = makeUserContext(userId, username, providedPassword);

                userContextToUserIdMap.put(username, userContext);
                verifiedUsernameToPassword.put(username, providedPassword);

                resultHandler.handle(Future.succeededFuture(userContext));
                return;
            }

            resultHandler.handle(Future.failedFuture("Unauthorized"));
            return;
        }

        if (existingPassword.equals(providedPassword))
        {
            final User userContext = userContextToUserIdMap.get(username);
            resultHandler.handle(Future.succeededFuture(userContext));
            return;
        }

        resultHandler.handle(Future.failedFuture("Unauthorized"));
    }

    private User makeUserContext(final long userId, final String username, final String providedPassword)
    {
        return new User()
        {
            @Override
            public JsonObject attributes()
            {
                return new JsonObject(Map.of("userId", userId, "username", username, "password", providedPassword));
            }

            @Override
            public User isAuthorized(final Authorization authority, final Handler<AsyncResult<Boolean>> resultHandler)
            {
                resultHandler.handle(Future.succeededFuture(true));
                return new UserImpl();
            }

            @Override
            public JsonObject principal()
            {
                return null;
            }

            @Override
            public void setAuthProvider(final AuthProvider authProvider)
            {

            }
        };
    }
}
