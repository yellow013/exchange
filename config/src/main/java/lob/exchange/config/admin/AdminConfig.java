package lob.exchange.config.admin;

import lob.exchange.config.ConfigReader;
import lob.exchange.config.aeronic.AeronicConfig;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class AdminConfig extends AeronicConfig
{
    public AdminConfig(final PropertiesConfiguration configuration)
    {
        super(configuration);
    }

    public int getPort()
    {
        return configuration.getInt("port");
    }

    public static AdminConfig readConfig()
    {
        return new AdminConfig(ConfigReader.readConfig("admin.properties"));
    }
}
