package exchange.lob.node.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;


public class ExchangeNodeConfiguration
{
    public final int basePort;
    public final Map<Integer, String> nodes;

    @JsonCreator
    public ExchangeNodeConfiguration(
        final @JsonProperty("basePort") int basePort,
        final @JsonProperty("nodes") Map<Integer, String> nodes
    )
    {
        this.basePort = basePort;
        this.nodes = nodes;
    }

    public static ExchangeNodeConfiguration fromYaml(final String fileName)
    {
        try
        {
            final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
            final URL yamlResource = ClassLoader.getSystemResource(fileName);
            return objectMapper.readValue(yamlResource, ExchangeNodeConfiguration.class);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getAeronClusterConfig()
    {
        return ExchangeClusterUtil.clusterMembers(basePort, List.copyOf(nodes.values()));
    }

    public String getAeronClusterIngressEndpoints()
    {
        return ExchangeClusterUtil.ingressEndpoints(basePort, List.copyOf(nodes.values()));
    }
}
