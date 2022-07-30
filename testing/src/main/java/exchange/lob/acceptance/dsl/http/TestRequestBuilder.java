package exchange.lob.acceptance.dsl.http;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TestRequestBuilder
{
    final HttpRequest.Builder builder = HttpRequest.newBuilder();

    public HttpRequest build()
    {
        return builder.build();
    }

    public TestRequestBuilder GET()
    {
        builder.method("GET", HttpRequest.BodyPublishers.noBody());
        return this;
    }

    public TestRequestBuilder POST(final String body)
    {
        builder.method("POST", HttpRequest.BodyPublishers.ofString(body));
        return this;
    }

    public TestRequestBuilder basicAuth(final String username, final String password)
    {
        final String authString = username + ":" + password;
        final byte[] encodedAuthString = Base64.getEncoder().encode(authString.getBytes(StandardCharsets.ISO_8859_1));
        final String authHeader = "Basic " + new String(encodedAuthString);
        builder.header("Authorization", authHeader);
        return this;
    }

    public TestRequestBuilder uri(final String uri)
    {
        builder.uri(URI.create(uri));
        return this;
    }
}
