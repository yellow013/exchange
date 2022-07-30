package lob.exchange.config.rest;

import lob.exchange.config.ConfigReader;
import lob.exchange.config.aeronic.AeronicConfig;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class RestApiConfig extends AeronicConfig
{
    public RestApiConfig(final PropertiesConfiguration configuration)
    {
        super(configuration);
    }

    public int getPort()
    {
        return configuration.getInt("port");
    }

    public static RestApiConfig readConfig()
    {
        return new RestApiConfig(ConfigReader.readConfig("rest-api.properties"));
    }
}
