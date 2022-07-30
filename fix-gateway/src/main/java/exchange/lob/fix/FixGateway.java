package exchange.lob.fix;

import exchange.lob.api.codecs.fix.FixDictionaryImpl;
import exchange.lob.events.admin.AdminClient;
import exchange.lob.events.trading.TradingRequests;
import exchange.lob.node.util.FileUtil;
import exchange.lob.user.User;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.ArchivingMediaDriver;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import lob.exchange.config.fix.FixGatewayConfig;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.status.AtomicCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.artio.CommonConfiguration;
import uk.co.real_logic.artio.dictionary.generation.Exceptions;
import uk.co.real_logic.artio.engine.EngineConfiguration;
import uk.co.real_logic.artio.engine.FixEngine;
import uk.co.real_logic.artio.session.Session;
import uk.co.real_logic.artio.validation.AuthenticationStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static exchange.lob.fix.util.MediaDriverUtil.cleanupMediaDriver;
import static exchange.lob.fix.util.Util.cleanupOldLogFileDir;
import static io.aeron.CommonContext.IPC_CHANNEL;
import static uk.co.real_logic.artio.CommonConfiguration.backoffIdleStrategy;

public class FixGateway
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FixGateway.class);

    private final EngineConfiguration engineConfiguration;
    private final MediaDriver.Context context;
    private final Archive.Context archiveContext;
    private final String aeronDirName;
    private final TradingRequests tradingRequests;
    private ArchivingMediaDriver archivingMediaDriver;
    private FixEngine engine;
    private AgentRunner runner;

    private final Map<Long, Session> fixSessionByUserId;
    private final Map<String, User> userByUserId = new ConcurrentHashMap<>();

    public FixGateway(
        final String hostname,
        final FixGatewayConfig fixGatewayConfig,
        final AdminClient adminClient,
        final TradingRequests tradingRequests,
        final Map<Long, Session> fixSessionByUserId
    )
    {
        this.tradingRequests = tradingRequests;
        this.fixSessionByUserId = fixSessionByUserId;

        final AuthenticationStrategy authenticationStrategy = new ExchangeAuthenticationStrategy(adminClient, userByUserId);

        this.aeronDirName = FileUtil.tmpDirForName("fix-gateway-engine");
        final String mediaDriverDir = aeronDirName + "-driver";
        final String archiveDir = aeronDirName + "-archive";
        final String logsDir = aeronDirName + "-logs";

        final EngineConfiguration engineConfiguration = new EngineConfiguration()
            .bindTo(hostname, fixGatewayConfig.getPort())
            .libraryAeronChannel(IPC_CHANNEL)
            .logFileDir(logsDir)
            .authenticationStrategy(authenticationStrategy)
            .acceptorfixDictionary(FixDictionaryImpl.class);

        engineConfiguration
            .aeronContext()
            .aeronDirectoryName(mediaDriverDir);

        engineConfiguration
            .aeronArchiveContext()
            .aeronDirectoryName(archiveDir);

        this.engineConfiguration = engineConfiguration;

        cleanupOldLogFileDir(engineConfiguration);

        this.context = new MediaDriver.Context()
            .aeronDirectoryName(mediaDriverDir)
            .threadingMode(ThreadingMode.SHARED)
            .sharedIdleStrategy(backoffIdleStrategy())
            .dirDeleteOnStart(true);

        this.archiveContext = new Archive.Context()
            .aeronDirectoryName(context.aeronDirectoryName())
            .archiveDirectoryName(archiveDir)
            .threadingMode(ArchiveThreadingMode.SHARED)
            .idleStrategySupplier(CommonConfiguration::backoffIdleStrategy)
            .deleteArchiveOnStart(true);
    }

    public void close()
    {
        Exceptions.closeAll(
            () -> {
                LOGGER.info("closing engine");
                engine.close();
            },
            () -> {
                LOGGER.info("stopping runner (library)");
                runner.close();
            },
            () -> {
                LOGGER.info("cleaning up media driver");
                cleanupMediaDriver(archivingMediaDriver);
            }
        );

        LOGGER.info("FIX engine stopped");
    }

    public void runAgent(final Agent agent)
    {
        final AtomicCounter errorCounter = archivingMediaDriver.mediaDriver()
            .context()
            .countersManager()
            .newCounter("exchange_agent_errors");

        this.runner = new AgentRunner(
            CommonConfiguration.backoffIdleStrategy(),
            Throwable::printStackTrace,
            errorCounter,
            agent
        );

        AgentRunner.startOnThread(runner);
    }

    public void start()
    {
        archivingMediaDriver = ArchivingMediaDriver.launch(context, archiveContext);
        engine = FixEngine.launch(engineConfiguration);
        runAgent(new FixGatewayAgent(tradingRequests, aeronDirName + "-driver", userByUserId, fixSessionByUserId));
    }
}