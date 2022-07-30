package lob.exchange.config;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigReader
{
    public static PropertiesConfiguration readConfig(final String configName)
    {
        try
        {
            final Configurations configurations = new Configurations();
            return configurations.properties(configName);
        }
        catch (final ConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
}
