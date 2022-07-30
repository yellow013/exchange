package exchange.lob.admin;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.events.admin.AdminRequests;
import exchange.lob.events.admin.AdminResponse;
import exchange.lob.node.client.ExchangeClusterPrincipal;
import exchange.lob.node.client.NoOpEgressListener;
import exchange.lob.node.util.ExchangeNodeConfiguration;
import exchange.lob.node.util.FileUtil;
import io.aeron.Aeron;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeronic.AeronicWizard;
import lob.exchange.config.admin.AdminConfig;
import lob.exchange.config.aeronic.AeronicConfig;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.aeron.CommonContext.UDP_CHANNEL;

public class AdminApiMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminApiMain.class);


    public static void main(String[] args)
    {
        final String hostname = args[0];
        final String configFile = args[1];

        final AdminConfig adminConfig = AdminConfig.readConfig();
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        final ExchangeNodeConfiguration configuration = ExchangeNodeConfiguration.fromYaml(configFile);

        LOGGER.info("Loaded config: {}", configuration);

        final MediaDriver mediaDriver = MediaDriver.launchEmbedded(
            new MediaDriver.Context()
                .aeronDirectoryName(FileUtil.tmpDirForName(ExchangeClusterPrincipal.ADMIN_API.name))
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.SHARED)
                .errorHandler(Throwable::printStackTrace)
        );

        final Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
        final AeronicWizard aeronic = new AeronicWizard(aeron);

        final AdminClient adminClient = createAdminExchangeClient(
            adminConfig,
            aeronic,
            mediaDriver.aeronDirectoryName(),
            configuration.getAeronClusterIngressEndpoints()
        );

        final AdminApi adminApi = new AdminApi(adminConfig, adminClient);

        aeronic.start();
        adminApi.start();

        LOGGER.info("Admin api started");

        SigInt.register(() -> {
            adminApi.close();
            aeronic.close();
            barrier.signal();
        });

        barrier.await();
    }

    private static AdminClient createAdminExchangeClient(
        final AdminConfig adminConfig,
        final AeronicWizard aeronic,
        final String aeronDirectoryName,
        final String clusterIngressEndpoints
    )
    {
        final AdminRequests adminRequests = aeronic.createClusterIngressPublisher(
            AdminRequests.class,
            new AeronCluster.Context()
                .egressListener(NoOpEgressListener.INSTANCE)
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
                .egressChannel(adminConfig.getChannel(AdminResponse.class))
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(clusterIngressEndpoints)
        );

        return adminClient;
    }
}
