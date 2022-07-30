package lob.exchange.config.marketdata;

import lob.exchange.config.ConfigReader;
import lob.exchange.config.aeronic.AeronicConfig;
import org.apache.commons.configuration2.PropertiesConfiguration;

public class MarketDataWebsocketServerConfig extends AeronicConfig
{

    public MarketDataWebsocketServerConfig(final PropertiesConfiguration config)
    {
        super(config);
    }

    public int getPort()
    {
        return configuration.getInt("port");
    }

    public static MarketDataWebsocketServerConfig readConfig()
    {
        return new MarketDataWebsocketServerConfig(ConfigReader.readConfig("market-data-ws.properties"));
    }
}
