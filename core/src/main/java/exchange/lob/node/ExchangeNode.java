package exchange.lob.node;

import exchange.lob.events.admin.AdminRequestProcessor;
import exchange.lob.events.admin.AdminRequests;
import exchange.lob.events.admin.AdminResponse;
import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.events.trading.TradingRequestProcessor;
import exchange.lob.events.trading.TradingRequests;
import exchange.lob.node.util.ExchangeNodeConfiguration;
import exchange.lob.node.util.FileUtil;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MinMulticastFlowControlSupplier;
import io.aeron.driver.ThreadingMode;
import io.aeronic.cluster.AeronicClusteredServiceContainer;
import io.aeronic.cluster.AeronicClusteredServiceRegistry;
import lob.exchange.config.aeronic.AeronicConfig;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static exchange.lob.node.util.ExchangeClusterUtil.*;

public class ExchangeNode
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeNode.class);

    private final ShutdownSignalBarrier barrier;
    private ClusteredMediaDriver clusteredMediaDriver;
    private ClusteredServiceContainer container;

    private File clusterDir;
    private ExchangeClusteredService service;

    public ExchangeNode(ShutdownSignalBarrier barrier)
    {
        this.barrier = barrier;
    }

    public void start(final int nodeId, final ExchangeNodeConfiguration exchangeNodeConfiguration, final boolean cleanStart)
    {
        final String nodeHostname = exchangeNodeConfiguration.nodes.get(nodeId);
        final String aeronDir = FileUtil.tmpDirForName("exchange-cluster-node-" + nodeId);
        final String baseDir = FileUtil.tmpDirForName("exchange-cluster-driver-" + nodeId);

        LOGGER.info("Aeron Dir = {}", aeronDir);
        LOGGER.info("Cluster Dir = {}", baseDir);

        final int basePort = exchangeNodeConfiguration.basePort;
        final String archiveControlChannel = udpChannel(nodeId, nodeHostname, basePort, ARCHIVE_CONTROL_REQUEST_PORT_OFFSET);
        final String archiveLogControlChannel = "aeron:ipc?term-length=64k";
        final String clusterConfig = exchangeNodeConfiguration.getAeronClusterConfig();
        final String ingressChannel = "aeron:udp?term-length=64k";
        final String replicationChannel = udpChannel(nodeId, nodeHostname, 40000, 1);
        final String consensusModuleLogChannel = logControlChannel(nodeId, nodeHostname, basePort, LOG_CONTROL_PORT_OFFSET);

        LOGGER.info("Archive control channel: {}", archiveControlChannel);
        LOGGER.info("Archive log control channel: {}", archiveLogControlChannel);
        LOGGER.info("Cluster config: {}", clusterConfig);
        LOGGER.info("Ingress channel: {}", ingressChannel);
        LOGGER.info("Cluster log channel: {}", consensusModuleLogChannel);

        final MediaDriver.Context mediaDriverContext = new MediaDriver.Context();
        final ConsensusModule.Context consensusModuleContext = new ConsensusModule.Context();
        final Archive.Context archiveContext = new Archive.Context();
        final ClusteredServiceContainer.Context serviceContainerContext = new ClusteredServiceContainer.Context();
        final AeronicClusteredServiceContainer aeronicClusteredServiceContainer = createClusteredService();

        this.clusterDir = new File(baseDir, "consensus-module");

        mediaDriverContext
            .aeronDirectoryName(aeronDir)
            .threadingMode(ThreadingMode.SHARED)
            .termBufferSparseFile(true)
            .multicastFlowControlSupplier(new MinMulticastFlowControlSupplier())
            .terminationHook(barrier::signal)
            .dirDeleteOnStart(cleanStart);

        archiveContext
            .archiveDir(new File(baseDir, "archive"))
            .controlChannel(archiveControlChannel)
            .localControlChannel(archiveLogControlChannel)
            .recordingEventsEnabled(false)
            .deleteArchiveOnStart(cleanStart)
            .threadingMode(ArchiveThreadingMode.SHARED);

        consensusModuleContext
            .authenticatorSupplier(ExchangeAuthenticator::new)
            .sessionTimeoutNs(TimeUnit.MINUTES.toNanos(60))
            .errorHandler(Throwable::printStackTrace)
            .clusterMemberId(nodeId)
            .clusterMembers(clusterConfig)
            .aeronDirectoryName(aeronDir)
            .clusterDir(clusterDir)
            .ingressChannel(ingressChannel)
            .logChannel(consensusModuleLogChannel)
            .replicationChannel(replicationChannel)
            .deleteDirOnStart(cleanStart);

        serviceContainerContext
            .shutdownSignalBarrier(barrier)
            .aeronDirectoryName(aeronDir)
            .clusterDir(new File(baseDir, "service"))
            .clusteredService(aeronicClusteredServiceContainer)
            .errorHandler(Throwable::printStackTrace);

        clusteredMediaDriver = ClusteredMediaDriver.launch(
            mediaDriverContext,
            archiveContext,
            consensusModuleContext
        );

        container = ClusteredServiceContainer.launch(serviceContainerContext);
    }

    private AeronicClusteredServiceContainer createClusteredService()
    {
        final AeronicConfig aeronicConfig = AeronicConfig.readConfig();

        final AeronicClusteredServiceContainer.Configuration clusterConfiguration = new AeronicClusteredServiceContainer.Configuration();
        final AeronicClusteredServiceRegistry registry = clusterConfiguration.registry();

        clusterConfiguration.registerEgressPublisher(AdminResponse.class);
        clusterConfiguration.registerToggledEgressPublisher(
            OrderBookEvents.class,
            aeronicConfig.getChannel(OrderBookEvents.class),
            aeronicConfig.getStreamId(OrderBookEvents.class)
        );

        final AdminResponse adminResponsePublisher = registry.getPublisherFor(AdminResponse.class);
        final OrderBookEvents orderBookEvents = registry.getToggledPublisherFor(OrderBookEvents.class);

        final AdminRequestProcessor adminRequestProcessor = new AdminRequestProcessor(adminResponsePublisher);
        final TradingRequestProcessor tradingRequestProcessor = new TradingRequestProcessor(orderBookEvents);

        clusterConfiguration.registerIngressSubscriber(AdminRequests.class, adminRequestProcessor);
        clusterConfiguration.registerIngressSubscriber(TradingRequests.class, tradingRequestProcessor);

        this.service = new ExchangeClusteredService(adminRequestProcessor, tradingRequestProcessor, orderBookEvents);
        clusterConfiguration.clusteredService(service);

        return new AeronicClusteredServiceContainer(clusterConfiguration);
    }

    public void close()
    {
        LOGGER.info("Closing exchange cluster node of role: {}", getRole());
        container.close();
        clusteredMediaDriver.close();
    }

    public File getClusterDir()
    {
        return clusterDir;
    }

    public Cluster.Role getRole()
    {
        return service.getRole();
    }

    public int getMemberId()
    {
        return service.getMemberId();
    }
}
