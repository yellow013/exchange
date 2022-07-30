package exchange.lob.acceptance.dsl.fix;

import com.lmax.simpledsl.DslParams;
import com.lmax.simpledsl.OptionalParam;
import com.lmax.simpledsl.RequiredParam;
import exchange.lob.acceptance.TestStorage;
import exchange.lob.fix.fields.*;
import org.agrona.collections.MutableLong;

import java.math.BigDecimal;
import java.util.Optional;


public class FixSessionDsl
{
    private final TestStorage testStorage;
    private final FixSessionDriver driver;
    private final MutableLong clientOrderIdCounter = new MutableLong(-1L);
    private final String sessionUsername;

    public FixSessionDsl(final String username, final TestStorage testStorage, final String gatewayHostname)
    {
        this.testStorage = testStorage;
        this.sessionUsername = testStorage.getSystemUsername(username);
        this.driver = new FixSessionDriver(sessionUsername, gatewayHostname, 9999);
    }

    public void login(final String password, final boolean authenticationFailure)
    {
        driver.login(sessionUsername, password, authenticationFailure);
    }

    public void placeOrder(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new OptionalParam("clientOrderId"),
            new RequiredParam("product"),
            new RequiredParam("ordType"),
            new RequiredParam("side").setAllowedValues("Buy", "Sell"),
            new OptionalParam("price"),
            new OptionalParam("orderQty")
        );

        final String clientOrderId = params.valueAsOptional("clientOrderId").orElse(String.valueOf(clientOrderIdCounter.incrementAndGet()));
        final String product = testStorage.getSystemProduct(params.value("product"));
        final Optional<String> maybePrice = params.valueAsOptional("price");
        final Optional<String> maybeOrderQty = params.valueAsOptional("orderQty");

        final OrdType ordType = OrdType.valueOf(params.value("ordType"));
        final Side side = Side.valueOf(params.value("side"));

        driver.placeOrder(clientOrderId, product, ordType, side, maybePrice, maybeOrderQty);
    }

    public void cancelOrder(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("clientOrderId"),
            new RequiredParam("product"),
            new RequiredParam("orderQty")
        );

        final String clientOrderId = params.valueAsOptional("clientOrderId").orElse(String.valueOf(clientOrderIdCounter.incrementAndGet()));
        final String product = testStorage.getSystemProduct(params.value("product"));
        final Optional<BigDecimal> maybeOrderQty = params.valueAsOptional("orderQty").map(BigDecimal::new);

        driver.cancelOrder(clientOrderId, product, maybeOrderQty);
    }

    public void verifyExecutionReport(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("clientOrderId"),
            new RequiredParam("product"),
            new RequiredParam("ordStatus"),
            new RequiredParam("execType"),
            new RequiredParam("side").setAllowedValues("Buy", "Sell"),
            new OptionalParam("price"),
            new OptionalParam("orderQty"),
            new OptionalParam("rejectionReason"),
            new OptionalParam("text")
        );

        final String clientOrderId = params.value("clientOrderId");
        final OrdStatus ordStatus = OrdStatus.valueOf(params.value("ordStatus"));
        final ExecType execType = ExecType.valueOf(params.value("execType"));
        final String product = testStorage.getSystemProduct(params.value("product"));

        final Side side = Side.valueOf(params.value("side"));
        final Optional<OrdRejReason> rejectionReason = params.valueAsOptional("rejectionReason").map(OrdRejReason::valueOf);

        final Optional<String> maybePrice = params.valueAsOptional("price");
        final Optional<String> maybeOrderQty = params.valueAsOptional("orderQty");

        final Optional<String> text = params.valueAsOptional("text");

        final ExecutionReport expectedExecutionReport = new ExecutionReport(
            product,
            clientOrderId,
            ordStatus,
            execType,
            side,
            maybePrice,
            maybeOrderQty,
            rejectionReason,
            text
        );

        driver.verifyExecutionReport(expectedExecutionReport);
    }

    public void noMoreExecutionReports()
    {
        driver.noMoreExecutionReports();
    }

    public void noMoreCancelRejects()
    {
        driver.noMoreCancelRejects();
    }

    public void verifyCancelReject(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("clientOrderId"),
            new RequiredParam("rejectionReason")
        );

        final String clientOrderId = params.value("clientOrderId");
        final CxlRejReason cxlRejReason = CxlRejReason.valueOf(params.value("rejectionReason"));

        final FixOrderCancelReject expectedOrderCancelReject = new FixOrderCancelReject(clientOrderId, cxlRejReason);

        driver.verifyCancelReject(expectedOrderCancelReject);
    }

    public void logout()
    {
        driver.logout();
    }

    public void close()
    {
        driver.close();
    }
}
