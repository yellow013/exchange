package exchange.lob.acceptance;

import exchange.lob.acceptance.dsl.admin.AdminApiDsl;
import exchange.lob.acceptance.dsl.fix.FixClientDsl;
import exchange.lob.acceptance.dsl.fix.FixSessionDsl;
import exchange.lob.acceptance.dsl.md.MarketDataWebSocketServerDsl;
import exchange.lob.acceptance.dsl.rest.RestAPIDsl;
import exchange.lob.acceptance.dsl.ui.admin.AdminUIDsl;
import exchange.lob.acceptance.dsl.ui.trading.TradingUIDsl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static exchange.lob.node.util.ExchangeClusterUtil.LOCALHOST;

public class AcceptanceTestCase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptanceTestCase.class);

    protected AdminApiDsl admin;
    protected MarketDataWebSocketServerDsl mdws;
    protected RestAPIDsl restAPI;
    protected TradingUIDsl tradingUI;
    protected AdminUIDsl adminUI;
    protected FixClientDsl fix;
    protected TestStorage testStorage = new TestStorage();

    @BeforeEach
    public void setUp()
    {
        admin = new AdminApiDsl(testStorage);
        fix = new FixClientDsl(testStorage, LOCALHOST);
        mdws = new MarketDataWebSocketServerDsl(testStorage);
        restAPI = new RestAPIDsl(testStorage);
        tradingUI = new TradingUIDsl(testStorage);
        adminUI = new AdminUIDsl(testStorage);
    }

    @AfterEach
    public void tearDown()
    {
        LOGGER.info("In acceptance tearDown");
        mdws.close();
        fix.close();
    }

    public FixSessionDsl fix(final String username)
    {
        return fix.as(username);
    }
}
