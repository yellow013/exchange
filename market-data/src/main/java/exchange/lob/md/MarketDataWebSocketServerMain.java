package exchange.lob.md;

import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.node.client.ExchangeClusterPrincipal;
import exchange.lob.node.util.ExchangeNodeConfiguration;
import exchange.lob.node.util.FileUtil;
import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeronic.AeronicWizard;
import io.vertx.core.Vertx;
import lob.exchange.config.aeronic.AeronicConfig;
import lob.exchange.config.marketdata.MarketDataWebsocketServerConfig;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SystemEpochNanoClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketDataWebSocketServerMain
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataWebSocketServerMain.class);

    public static void main(final String[] args)
    {
        final String hostname = args[0];
        final String configFile = args[1];

        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        final ExchangeNodeConfiguration configuration = ExchangeNodeConfiguration.fromYaml(configFile);
        final MarketDataWebsocketServerConfig config = MarketDataWebsocketServerConfig.readConfig();

        LOGGER.info("Loaded config: {}", configuration);

        final SystemEpochNanoClock clock = new SystemEpochNanoClock();

        final MediaDriver mediaDriver = MediaDriver.launchEmbedded(
            new MediaDriver.Context()
                .aeronDirectoryName(FileUtil.shmDirForName(ExchangeClusterPrincipal.WS_SERVER.name))
                .dirDeleteOnStart(true)
                .threadingMode(ThreadingMode.SHARED)
                .spiesSimulateConnection(true)
                .errorHandler(Throwable::printStackTrace)
        );

        final MarketDataVerticle marketDataVerticle = new MarketDataVerticle(config);

        final Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
        final AeronicWizard aeronic = new AeronicWizard(aeron);
        final AeronicConfig aeronicConfig = AeronicConfig.readConfig();

        aeronic.registerSubscriber(
            OrderBookEvents.class,
            new MarketDataOrderBookEvents(marketDataVerticle, clock),
            aeronicConfig.getChannel(OrderBookEvents.class),
            aeronicConfig.getStreamId(OrderBookEvents.class)
        );

        aeronic.start();

        deployVerticle(marketDataVerticle);

        SigInt.register(() -> {
            closeVerticle(marketDataVerticle);
            mediaDriver.close();
            aeronic.close();
            barrier.signal();
        });

        barrier.await();
    }

    private static void closeVerticle(final MarketDataVerticle marketDataVerticle)
    {
        try
        {
            marketDataVerticle.stop();
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void deployVerticle(final MarketDataVerticle marketDataVerticle)
    {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(marketDataVerticle);
    }
}
