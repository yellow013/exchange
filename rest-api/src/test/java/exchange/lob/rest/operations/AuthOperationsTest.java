package exchange.lob.rest.operations;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.Test;

public class AuthOperationsTest extends AcceptanceTestCase
{

    @Test
    public void shouldBeAbleToAuthExistingUser()
    {
        admin.addUser("username: trader", "password: strongPassword");

        restAPI.authUser("username: trader", "password: strongPassword", "expectedStatusCode: 200", "expectedBody: OK");
    }


    @Test
    public void shouldNotAuthUserWithWrongPassword()
    {
        admin.addUser("username: trader", "password: strongPassword");

        restAPI.authUser("username: trader", "password: wrongPassword", "expectedStatusCode: 401", "expectedBody: Unauthorized");
    }
}
