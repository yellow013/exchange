package exchange.lob.rest;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.events.admin.AdminRequests;
import exchange.lob.events.admin.AdminResponse;
import exchange.lob.events.trading.TradingRequests;
import exchange.lob.node.client.ExchangeClusterPrincipal;
import exchange.lob.node.client.NoOpEgressListener;
import exchange.lob.node.util.ExchangeNodeConfiguration;
import exchange.lob.node.util.FileUtil;
import io.aeron.Aeron;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeronic.AeronicWizard;
import io.vertx.core.Vertx;
import lob.exchange.config.rest.RestApiConfig;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.aeron.CommonContext.UDP_CHANNEL;

public class RestApiMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiMain.class);

    public static void main(final String... args)
    {
        final String hostname = args[0];
        final String configFile = args[1];
        final String openApiPath = args[2];

        final RestApiConfig restApiConfig = RestApiConfig.readConfig();
        final ExchangeNodeConfiguration configuration = ExchangeNodeConfiguration.fromYaml(configFile);

        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

        final MediaDriver mediaDriver = MediaDriver.launchEmbedded(
            new MediaDriver.Context()
                .aeronDirectoryName(FileUtil.tmpDirForName(ExchangeClusterPrincipal.REST_API.name))
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.SHARED)
                .errorHandler(Throwable::printStackTrace)
        );

        final Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
        final AeronicWizard aeronic = new AeronicWizard(aeron);

        final AdminClient adminClient = createAdminExchangeClient(
            restApiConfig,
            aeronic,
            mediaDriver.aeronDirectoryName(),
            configuration.getAeronClusterIngressEndpoints()
        );

        final TradingRequests tradingRequests = aeronic.createClusterIngressPublisher(
            TradingRequests.class,
            new AeronCluster.Context()
                .egressListener(NoOpEgressListener.INSTANCE)
                .messageTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(configuration.getAeronClusterIngressEndpoints())
        );

        aeronic.start();
        aeronic.awaitUntilPubsAndSubsConnect();

        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RestApiVerticle(openApiPath, tradingRequests, adminClient)).result();

        LOGGER.info("Verticle deployed");

        SigInt.register(() -> {
            vertx.close().result();
            aeronic.close();
            barrier.signal();
        });

        barrier.await();
    }

    private static AdminClient createAdminExchangeClient(
        final RestApiConfig restApiConfig,
        final AeronicWizard aeronic,
        final String aeronDirectoryName,
        final String clusterIngressEndpoints
    )
    {
        final AdminRequests adminRequests = aeronic.createClusterIngressPublisher(
            AdminRequests.class,
            new AeronCluster.Context()
                .messageTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .aeronDirectoryName(aeronDirectoryName)
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(clusterIngressEndpoints)
        );

        final AdminClient adminClient = new AdminClient(adminRequests);

        aeronic.registerClusterEgressSubscriber(
            AdminResponse.class,
            adminClient,
            new AeronCluster.Context()
                .messageTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .aeronDirectoryName(aeronDirectoryName)
                .egressChannel(restApiConfig.getChannel(AdminResponse.class))
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(clusterIngressEndpoints)
        );

        return adminClient;
    }
}
