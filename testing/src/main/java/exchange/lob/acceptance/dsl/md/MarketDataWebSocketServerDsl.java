package exchange.lob.acceptance.dsl.md;

import com.lmax.simpledsl.DslParams;
import com.lmax.simpledsl.OptionalParam;
import com.lmax.simpledsl.RequiredParam;
import exchange.lob.acceptance.TestStorage;
import exchange.lob.api.codecs.internal.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class MarketDataWebSocketServerDsl
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataWebSocketServerDsl.class);

    private final MarketDataWebSocketDriver driver;
    private final TestStorage testStorage;

    public MarketDataWebSocketServerDsl(final TestStorage testStorage)
    {
        this.testStorage = testStorage;
        this.driver = new MarketDataWebSocketDriver(URI.create("ws://localhost:8082"));
    }

    public void connect()
    {
        driver.connect();
        driver.waitForWebsocketToOpen();
        LOGGER.info("Started acceptance market data driver");
    }

    public void verifyPlaceOrderUpdateReceived(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("product"),
            new RequiredParam("side"),
            new OptionalParam("price"),
            new RequiredParam("amount")
        );

        final String productSymbol = testStorage.getSystemProduct(params.value("product"));
        final Side side = Side.valueOf(params.value("side"));
        final double price = params.valueAsDouble("price");
        final double amount = params.valueAsDouble("amount");

        driver.verifyPlaceOrderUpdateReceived(productSymbol, side, price, amount);
    }

    public void verifyCancelOrderUpdateReceived(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("product"),
            new RequiredParam("side"),
            new OptionalParam("price"),
            new RequiredParam("amount")
        );

        final String productSymbol = testStorage.getSystemProduct(params.value("product"));
        final Side side = Side.valueOf(params.value("side"));
        final double price = params.valueAsDouble("price");
        final double amount = params.valueAsDouble("amount");

        driver.verifyCancelOrderUpdateReceived(productSymbol, side, price, amount);
    }

    public void verifyTradeUpdateReceived(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("product"),
            new RequiredParam("takerSide"),
            new OptionalParam("price"),
            new RequiredParam("amount")
        );

        final String productSymbol = testStorage.getSystemProduct(params.value("product"));
        final Side takerSide = Side.valueOf(params.value("takerSide"));
        final double price = params.valueAsDouble("price");
        final double amount = params.valueAsDouble("amount");

        driver.verifyTradeUpdateReceived(productSymbol, takerSide, price, amount);
    }

    public void noMoreUpdates()
    {
        driver.noMoreUpdates();
    }

    public void close()
    {
        driver.close();
    }
}
