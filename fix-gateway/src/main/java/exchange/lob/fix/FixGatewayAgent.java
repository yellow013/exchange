package exchange.lob.fix;

import exchange.lob.events.trading.TradingRequests;
import exchange.lob.user.User;
import org.agrona.concurrent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.artio.library.*;
import uk.co.real_logic.artio.session.Session;
import uk.co.real_logic.artio.validation.MessageValidationStrategy;

import java.util.Map;

import static exchange.lob.fix.util.Util.blockingConnect;
import static io.aeron.CommonContext.IPC_CHANNEL;
import static java.util.Collections.singletonList;

public class FixGatewayAgent implements Agent
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FixGatewayAgent.class);

    private static final int FRAGMENT_LIMIT = 10;
    private final String aeronDirName;

    private final TradingRequests tradingRequests;
    private FixLibrary library;
    private final Map<String, User> usernameByUserId;
    private final Map<Long, Session> fixSessionByUserId;

    public FixGatewayAgent(
        final TradingRequests tradingRequests,
        final String aeronDirName,
        final Map<String, User> usernameByUserId,
        final Map<Long, Session> fixSessionByUserId
    )
    {
        this.tradingRequests = tradingRequests;
        this.aeronDirName = aeronDirName;
        this.usernameByUserId = usernameByUserId;
        this.fixSessionByUserId = fixSessionByUserId;
    }

    @Override
    public void onStart()
    {
        final LibraryConfiguration configuration = new LibraryConfiguration();

        configuration
            .aeronContext()
            .aeronDirectoryName(aeronDirName);

        configuration
            .libraryConnectHandler(new LibraryConnectHandler()
            {
                @Override
                public void onDisconnect(FixLibrary library)
                {
                    LOGGER.info("Library Disconnected");
                }

                @Override
                public void onConnect(FixLibrary library)
                {
                    LOGGER.info("Library Connected");
                }
            })
            .sessionAcquireHandler((session, acquiredInfo) -> onAcquire(session))
            .sessionExistsHandler(new AcquiringSessionExistsHandler(true))
            .libraryAeronChannels(singletonList(IPC_CHANNEL))
            .messageValidationStrategy(MessageValidationStrategy.none());

        library = blockingConnect(configuration);
    }

    @Override
    public void onClose()
    {
        LOGGER.info("In onClose of FixGatewayAgent");
        library.close();
    }

    private SessionHandler onAcquire(final Session session)
    {
        final long sessionUserId = usernameByUserId.get(session.compositeKey().remoteCompId()).getUserId();
        fixSessionByUserId.put(sessionUserId, session);
        return new FixSessionHandler(session, tradingRequests);
    }

    @Override
    public int doWork()
    {
        return library.poll(FRAGMENT_LIMIT);
    }

    @Override
    public String roleName()
    {
        return "Exchange";
    }
}

