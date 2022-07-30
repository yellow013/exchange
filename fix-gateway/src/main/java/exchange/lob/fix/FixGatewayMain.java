package exchange.lob.fix;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.events.admin.AdminRequests;
import exchange.lob.events.admin.AdminResponse;
import exchange.lob.events.trading.OrderBookEvents;
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
import lob.exchange.config.aeronic.AeronicConfig;
import lob.exchange.config.fix.FixGatewayConfig;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SystemEpochNanoClock;
import uk.co.real_logic.artio.session.Session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.aeron.CommonContext.UDP_CHANNEL;

public class FixGatewayMain
{
    public static void main(String[] args)
    {
        final String hostname = args[0];
        final String configFile = args[1];

        final FixGatewayConfig fixGatewayConfig = FixGatewayConfig.readConfig();
        final ExchangeNodeConfiguration configuration = ExchangeNodeConfiguration.fromYaml(configFile);
        final SystemEpochNanoClock clock = new SystemEpochNanoClock();

        final MediaDriver mediaDriver = MediaDriver.launchEmbedded(
            new MediaDriver.Context()
                .aeronDirectoryName(FileUtil.shmDirForName(ExchangeClusterPrincipal.FIX_GATEWAY.name))
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.SHARED)
                .spiesSimulateConnection(true)
                .errorHandler(Throwable::printStackTrace)
        );

        final Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
        final AeronicWizard aeronic = new AeronicWizard(aeron);

        final FixGateway fixGateway = createFixGateway(hostname, fixGatewayConfig, configuration, clock, mediaDriver, aeronic);

        aeronic.start();

        fixGateway.start();

        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

        SigInt.register(() -> {
            fixGateway.close();
            aeronic.close();
            barrier.signal();
        });

        barrier.await();
    }

    private static FixGateway createFixGateway(
        final String hostname,
        final FixGatewayConfig fixGatewayConfig,
        final ExchangeNodeConfiguration configuration,
        final SystemEpochNanoClock clock,
        final MediaDriver mediaDriver,
        final AeronicWizard aeronic
    )
    {
        final AeronicConfig aeronicConfig = AeronicConfig.readConfig();

        final AdminClient adminClient = createAdminExchangeClient(
            fixGatewayConfig,
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

        final ConcurrentHashMap<Long, Session> fixSessionByUserId = new ConcurrentHashMap<>();

        final FixGatewayOrderBookEvents orderBookEvents = new FixGatewayOrderBookEvents(fixSessionByUserId, clock);

        aeronic.registerSubscriber(
            OrderBookEvents.class,
            orderBookEvents,
            aeronicConfig.getChannel(OrderBookEvents.class),
            aeronicConfig.getStreamId(OrderBookEvents.class)
        );

        return new FixGateway(
            hostname,
            fixGatewayConfig,
            adminClient,
            tradingRequests,
            fixSessionByUserId
        );
    }

    private static AdminClient createAdminExchangeClient(
        final FixGatewayConfig fixGatewayConfig,
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
                .egressChannel(fixGatewayConfig.getChannel(AdminResponse.class))
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(clusterIngressEndpoints)
        );

        return adminClient;
    }
}
