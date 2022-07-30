package exchange.lob.node;

import exchange.lob.node.util.ExchangeNodeConfiguration;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeNodeMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeNodeMain.class);

    public static void main(String[] args)
    {
        final int nodeId = Integer.parseInt(args[0]);
        final String configFile = args[1];

        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        final ExchangeNode clusterNode = new ExchangeNode(barrier);
        final ExchangeNodeConfiguration nodeConfiguration = ExchangeNodeConfiguration.fromYaml(configFile);

        LOGGER.info("Loaded config: {}", nodeConfiguration);

        clusterNode.start(nodeId, nodeConfiguration, true);

        LOGGER.info("Cluster node started");

        SigInt.register(() -> {
            clusterNode.close();
            barrier.signal();
        });

        barrier.await();
    }
}
