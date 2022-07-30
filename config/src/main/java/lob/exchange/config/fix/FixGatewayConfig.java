package lob.exchange.config.fix;

import lob.exchange.config.ConfigReader;
import lob.exchange.config.aeronic.AeronicConfig;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class FixGatewayConfig extends AeronicConfig
{

    public FixGatewayConfig(final PropertiesConfiguration config)
    {
        super(config);
    }

    public int getPort()
    {
        return configuration.getInt("port");
    }

    public static FixGatewayConfig readConfig()
    {
        return new FixGatewayConfig(ConfigReader.readConfig("fix-gateway.properties"));
    }
}
