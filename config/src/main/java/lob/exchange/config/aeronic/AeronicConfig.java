package lob.exchange.config.aeronic;

import lob.exchange.config.ConfigReader;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class AeronicConfig
{
    protected final PropertiesConfiguration configuration;

    public AeronicConfig(final PropertiesConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public String getChannel(final Class<?> clazz)
    {
        final String propertyName = "%s.channel".formatted(clazz.getSimpleName());
        final String property = configuration.getString(propertyName);
        if (property == null)
        {
            throw new RuntimeException("Missing property: " + propertyName);
        }
        return property;
    }

    public int getStreamId(final Class<?> clazz)
    {
        return configuration.getInt("%s.streamId".formatted(clazz.getSimpleName()));
    }

    public static AeronicConfig readConfig()
    {
        return new AeronicConfig(ConfigReader.readConfig("aeronic.properties"));
    }
}
