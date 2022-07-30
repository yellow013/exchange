package lob.exchange.infra;

import exchange.lob.events.admin.AdminClient;
import exchange.lob.events.admin.AdminRequests;
import exchange.lob.events.admin.AdminResponse;
import exchange.lob.node.ExchangeNode;
import exchange.lob.node.client.ExchangeClusterPrincipal;
import exchange.lob.node.client.NoOpEgressListener;
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
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.aeron.CommonContext.UDP_CHANNEL;
import static java.util.stream.Collectors.toMap;
import static lob.exchange.infra.Util.awaitLeaderNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class ClusterResilienceTest
{
    private static final ExchangeNodeConfiguration multiNodeConfiguration = ExchangeNodeConfiguration.fromYaml("infra-multi-node.yaml");
    private AdminClient adminClient;
    private AeronicWizard aeronic;

    @Test
    public void shouldBeAbleToContinueAfterLeaderFailure() throws Exception
    {
        final ExchangeNode clusterNode = new ExchangeNode(new ShutdownSignalBarrier());
        clusterNode.start(0, multiNodeConfiguration, true);

        final ExchangeNode clusterNode1 = new ExchangeNode(new ShutdownSignalBarrier());
        clusterNode1.start(1, multiNodeConfiguration, true);

        final ExchangeNode clusterNode2 = new ExchangeNode(new ShutdownSignalBarrier());
        clusterNode2.start(2, multiNodeConfiguration, true);

        startClients();

        adminClient.addAsset("BTC", (byte)8).get(5, TimeUnit.SECONDS);

        final ExchangeNode leaderNode = awaitLeaderNode(clusterNode, clusterNode1, clusterNode2);

        ClusterTool.removeMember(leaderNode.getClusterDir(), leaderNode.getMemberId(), false);

        final ExchangeNode newLeader = awaitLeaderNode(clusterNode, clusterNode1, clusterNode2);

        assertNotSame(leaderNode, newLeader);

        adminClient.addAsset("ETH", (byte)18).get(5, TimeUnit.SECONDS);

        final Map<String, Asset> assets = getAssets();

        assertEquals(
            Map.of(
                "BTC", new Asset(1, "BTC", (byte) 8),
                "ETH", new Asset(2, "ETH", (byte) 18)
            ),
            assets
        );

        clusterNode.close();
        clusterNode1.close();
        clusterNode2.close();

        aeronic.close();
    }

    private void startClients()
    {
        MediaDriver mediaDriver = MediaDriver.launchEmbedded(
            new MediaDriver.Context()
                .aeronDirectoryName(FileUtil.tmpDirForName(ExchangeClusterPrincipal.ADMIN_API.name))
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.SHARED)
                .errorHandler(Throwable::printStackTrace)
        );

        final String aeronDirectoryName = mediaDriver.aeronDirectoryName();
        final Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDirectoryName));
        aeronic = new AeronicWizard(aeron);

        final AdminRequests adminRequests = aeronic.createClusterIngressPublisher(
            AdminRequests.class,
            new AeronCluster.Context()
                .messageTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .aeronDirectoryName(aeronDirectoryName)
                .ingressChannel(UDP_CHANNEL)
                .ingressEndpoints(multiNodeConfiguration.getAeronClusterIngressEndpoints())
                .egressListener(NoOpEgressListener.INSTANCE) // cluster CURRENTLY sends out messages to all connected sessions
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
                .ingressEndpoints(multiNodeConfiguration.getAeronClusterIngressEndpoints())
        );

        aeronic.start();
        aeronic.awaitUntilPubsAndSubsConnect();
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
}
