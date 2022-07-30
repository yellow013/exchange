package lob.exchange.infra;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.events.admin.AdminRequests;
import exchange.lob.events.admin.AdminResponse;
import exchange.lob.node.ExchangeNode;
import exchange.lob.node.client.ExchangeClusterPrincipal;
import exchange.lob.node.util.ExchangeNodeConfiguration;
import exchange.lob.node.util.FileUtil;
import exchange.lob.product.Asset;
import io.aeron.Aeron;
import io.aeron.cluster.ClusterTool;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeronic.AeronicWizard;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.aeron.CommonContext.UDP_CHANNEL;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClusterOperationTest
{

    private static final ExchangeNodeConfiguration singleNodeConfiguration = ExchangeNodeConfiguration.fromYaml("infra-single-node.yaml");

    private ExchangeNode clusterNode;
    private AeronicWizard aeronic;
    private AdminClient adminClient;
    private Aeron aeron;
    private MediaDriver mediaDriver;

    @AfterEach
    void tearDown()
    {
        aeronic.close();
        aeron.close();
        mediaDriver.close();
        clusterNode.close();
    }

    @Test
    public void shouldBeAbleToRestartFromExistingLogBuffers()
    {
        startNodeAndClient(true);

        adminClient.addAsset("BTC", (byte) 8).join();

        closeNodeAndClient();

        startNodeAndClient(false);

        adminClient.addAsset("ETH", (byte) 18).join();

        final Map<String, Asset> assetsAfterRestart = getAssets();

        assertEquals(
            Map.of(
                "BTC", new Asset(1, "BTC", (byte) 8),
                "ETH", new Asset(2, "ETH", (byte) 18)
            ),
            assetsAfterRestart
        );
    }

    @Test
    public void shouldBeAbleToRestartFromSnapshot()
    {
        startNodeAndClient(true);

        adminClient.addAsset("BTC", (byte) 8).join();

        ClusterTool.snapshot(clusterNode.getClusterDir(), System.out);

        closeNodeAndClient();

        startNodeAndClient(false);

        adminClient.addAsset("ETH", (byte) 18).join();

        final Map<String, Asset> assetsAfterRestart = getAssets();

        assertEquals(
            Map.of(
                "BTC", new Asset(1, "BTC", (byte) 8),
                "ETH", new Asset(2, "ETH", (byte) 18)
            ),
            assetsAfterRestart
        );
    }

    private Map<String, Asset> getAssets()
    {
        try
        {
            return adminClient.fetchAssets().get(1, TimeUnit.SECONDS).assets().stream()
                .collect(toMap(Asset::getSymbol, Function.identity()));
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void startNodeAndClient(final boolean cleanStart)
    {
        startNode(cleanStart);
        startClient(cleanStart);
    }

    private void startClient(final boolean cleanStart)
    {
        mediaDriver = MediaDriver.launchEmbedded(
            new MediaDriver.Context()
                .aeronDirectoryName(FileUtil.tmpDirForName(ExchangeClusterPrincipal.ADMIN_API.name))
                .dirDeleteOnStart(cleanStart)
                .threadingMode(ThreadingMode.SHARED)
                .errorHandler(Throwable::printStackTrace)
        );

        final String aeronDirectoryName = mediaDriver.aeronDirectoryName();
        aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDirectoryName));
        aeronic = new AeronicWizard(aeron);

        final AdminRequests adminRequests = aeronic.createClusterIngressPublisher(
            AdminRequests.class,
            new AeronCluster.Context()
                .messageTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .aeronDirectoryName(aeronDirectoryName)
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(singleNodeConfiguration.getAeronClusterIngressEndpoints())
        );

        adminClient = new AdminClient(adminRequests);

        aeronic.registerClusterEgressSubscriber(
            AdminResponse.class,
            adminClient,
            new AeronCluster.Context()
                .messageTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .aeronDirectoryName(aeronDirectoryName)
                .egressChannel("aeron:udp?endpoint=localhost:41450|reliable=true")
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(singleNodeConfiguration.getAeronClusterIngressEndpoints())
        );

        aeronic.start();
        aeronic.awaitUntilPubsAndSubsConnect();
    }

    private void startNode(final boolean cleanStart)
    {
        clusterNode = new ExchangeNode(new ShutdownSignalBarrier());
        clusterNode.start(0, singleNodeConfiguration, cleanStart);
    }

    private void closeNodeAndClient()
    {
        clusterNode.close();
        aeronic.close();
        aeron.close();
        mediaDriver.close();
    }
}
