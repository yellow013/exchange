package lob.exchange.infra;

import exchange.lob.node.ExchangeNode;
import io.aeron.cluster.service.Cluster;
import org.agrona.collections.MutableReference;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class Util
{

    public static ExchangeNode awaitLeaderNode(final ExchangeNode... clusterNodes)
    {
        final MutableReference<ExchangeNode> leaderNode = new MutableReference<>();
        await().timeout(20, TimeUnit.SECONDS)
            .pollDelay(1, TimeUnit.SECONDS)
            .then()
            .until(() -> {
                final Optional<ExchangeNode> maybeLeader = Arrays.stream(clusterNodes)
                    .filter(node -> node.getRole() == Cluster.Role.LEADER)
                    .findFirst();

                if (maybeLeader.isPresent())
                {
                    leaderNode.set(maybeLeader.get());
                    return true;
                }

                return false;
            });

        return leaderNode.get();
    }
}
