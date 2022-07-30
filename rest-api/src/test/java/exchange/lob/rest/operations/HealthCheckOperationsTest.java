package exchange.lob.rest.operations;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.Test;

public class HealthCheckOperationsTest extends AcceptanceTestCase
{

    @Test
    public void shouldBeAbleToHealthCheck()
    {
        restAPI.healthcheck("expectedStatusCode: 200", "expectedBody: OK");
    }
}
