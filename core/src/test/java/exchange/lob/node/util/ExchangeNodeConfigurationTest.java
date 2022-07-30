package exchange.lob.node.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExchangeNodeConfigurationTest
{

    public static final String CLUSTER_NODE_CONFIG_YAML = "cluster-node-config.local.yaml";

    @Test
    public void shouldReadClusterConfigurationFromFile()
    {
        ExchangeNodeConfiguration configuration = ExchangeNodeConfiguration.fromYaml(CLUSTER_NODE_CONFIG_YAML);
        assertThat(configuration.nodes).containsOnly(
            entry(0, "127.0.0.1"),
            entry(1, "127.0.0.1"),
            entry(2, "127.0.0.1"),
            entry(3, "127.0.0.1")
        );
    }

    @Test
    public void shouldProduceAeronClusterConfigString()
    {
        ExchangeNodeConfiguration configuration = ExchangeNodeConfiguration.fromYaml(CLUSTER_NODE_CONFIG_YAML);
        String expected =
            "0,127.0.0.1:9003,127.0.0.1:9004,127.0.0.1:9005,127.0.0.1:9006,127.0.0.1:9001|" +
                "1,127.0.0.1:9103,127.0.0.1:9104,127.0.0.1:9105,127.0.0.1:9106,127.0.0.1:9101|" +
                "2,127.0.0.1:9203,127.0.0.1:9204,127.0.0.1:9205,127.0.0.1:9206,127.0.0.1:9201|" +
                "3,127.0.0.1:9303,127.0.0.1:9304,127.0.0.1:9305,127.0.0.1:9306,127.0.0.1:9301|";

        String actual = configuration.getAeronClusterConfig();
        assertEquals(expected, actual);
    }

    @Test
    public void shouldProduceAeronIngressEndpointsString()
    {
        ExchangeNodeConfiguration configuration = ExchangeNodeConfiguration.fromYaml(CLUSTER_NODE_CONFIG_YAML);
        String expected = "0=127.0.0.1:9003,1=127.0.0.1:9103,2=127.0.0.1:9203,3=127.0.0.1:9303";
        String actual = configuration.getAeronClusterIngressEndpoints();
        assertEquals(expected, actual);
    }
}