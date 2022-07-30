package exchange.lob.fix;

import exchange.lob.events.trading.TradingRequests;
import io.aeron.logbuffer.ControlledFragmentHandler.Action;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.artio.library.OnMessageInfo;
import uk.co.real_logic.artio.library.SessionHandler;
import uk.co.real_logic.artio.messages.DisconnectReason;
import uk.co.real_logic.artio.session.Session;

import static io.aeron.logbuffer.ControlledFragmentHandler.Action.CONTINUE;

public class FixSessionHandler implements SessionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FixSessionHandler.class);

    private final ExchangeMessageDispatcher exchangeMessageDispatcher;

    FixSessionHandler(
        final Session session,
        final TradingRequests tradingRequests
    )
    {
        this.exchangeMessageDispatcher = new ExchangeMessageDispatcher(tradingRequests, session.compositeKey().remoteCompId());
    }

    @Override
    public Action onMessage(
        final DirectBuffer buffer,
        final int offset,
        final int length,
        final int libraryId,
        final Session session,
        final int sequenceIndex,
        final long messageType,
        final long timestampInNs,
        final long position,
        final OnMessageInfo messageInfo
    )
    {
        exchangeMessageDispatcher.dispatchToExchange(buffer, offset, length, messageType);
        return CONTINUE;
    }

    @Override
    public Action onDisconnect(final int libraryId, final Session session, final DisconnectReason reason)
    {
        LOGGER.info("FIX Session is on disconnect. Session {}", session);
        return CONTINUE;
    }

    @Override
    public void onSessionStart(final Session session)
    {

    }

    @Override
    public void onTimeout(final int libraryId, final Session session)
    {

    }

    @Override
    public void onSlowStatus(final int libraryId, final Session session, final boolean hasBecomeSlow)
    {

    }
}
