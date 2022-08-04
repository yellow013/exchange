package exchange.lob.rest;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.events.trading.TradingRequests;
import exchange.lob.rest.auth.ExchangeUserAuthenticationProvider;
import exchange.lob.rest.operations.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import lob.exchange.config.rest.RestApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class RestApiVerticle extends AbstractVerticle
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiVerticle.class);

    private final String openApiPath;
    private final TradingRequests tradingRequests;
    private final AdminClient adminClient;
    private final RestApiConfig restApiConfig;

    public RestApiVerticle(final String openApiPath, final TradingRequests tradingRequests, final AdminClient adminClient, final RestApiConfig restApiConfig)
    {
        this.openApiPath = openApiPath;
        this.tradingRequests = tradingRequests;
        this.adminClient = adminClient;
        this.restApiConfig = restApiConfig;
    }

    @Override
    public void start(final Promise<Void> startPromise)
    {
        RouterBuilder.create(vertx, openApiPath)
            .onFailure(Throwable::printStackTrace)
            .map(this::registerSecurity)
            .map(this::registerOperations)
            .map(this::registerCorsPolicy)
            .onSuccess(routerBuilder -> {
                final Router router = routerBuilder.createRouter();
                final HttpServer server = vertx
                    .createHttpServer(new HttpServerOptions().setPort(restApiConfig.getPort()).setHost("0.0.0.0"))
                    .requestHandler(router);

                server.listen()
                    .onSuccess(s -> LOGGER.info("Server started on port {}", s.actualPort()))
                    .onFailure(Throwable::printStackTrace);

                startPromise.complete();
            });
    }

    @Override
    public void stop(final Promise<Void> stopPromise)
    {
        stopPromise.complete();
    }

    private RouterBuilder registerOperations(final RouterBuilder routerBuilder)
    {
        final HealthCheckOperations healthCheckOperations = new HealthCheckOperations();
        final AuthOperations authOperations = new AuthOperations();
        final BalanceOperations balanceOperations = new BalanceOperations(adminClient);
        final ProductOperations productOperations = new ProductOperations(adminClient);
        final TradingOperations tradingOperations = new TradingOperations(tradingRequests);

        Stream.of(
            healthCheckOperations,
            authOperations,
            balanceOperations,
            productOperations,
            tradingOperations
        ).forEach(op -> op.wire(routerBuilder));

        return routerBuilder;
    }

    private RouterBuilder registerSecurity(final RouterBuilder routerBuilder)
    {
        return routerBuilder
            .securityHandler("basicAuth")
            .bindBlocking(config -> BasicAuthHandler.create(new ExchangeUserAuthenticationProvider(adminClient)));
    }

    private RouterBuilder registerCorsPolicy(final RouterBuilder routerBuilder)
    {
        return routerBuilder.rootHandler(
            CorsHandler.create(".*.")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowCredentials(true)
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Authorization")
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Content-Type")
        );
    }
}
