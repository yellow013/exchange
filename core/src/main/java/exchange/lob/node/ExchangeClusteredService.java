package exchange.lob.node;

import exchange.lob.Exchange;
import exchange.lob.api.sbe.ExchangeStateDecoder;
import exchange.lob.api.sbe.ExchangeStateEncoder;
import exchange.lob.events.admin.AdminRequestProcessor;
import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.events.trading.TradingRequestProcessor;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.client.ClusterException;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableBoolean;
import org.agrona.concurrent.IdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExchangeClusteredService implements ClusteredService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeClusteredService.class);

    private final MutableDirectBuffer snapshotBuffer = new ExpandableDirectByteBuffer(4096);
    private final ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder();
    private final ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder();
    private final AdminRequestProcessor adminRequestProcessor;

    private Exchange exchange;
    private final TradingRequestProcessor tradingRequestProcessor;
    private final OrderBookEvents orderBookEvents;
    private IdleStrategy idleStrategy;
    private Cluster cluster;

    private final AtomicBoolean terminated = new AtomicBoolean(false);

    public ExchangeClusteredService(
        final AdminRequestProcessor adminRequestProcessor,
        final TradingRequestProcessor tradingRequestProcessor,
        final OrderBookEvents orderBookEvents
    )
    {
        this.adminRequestProcessor = adminRequestProcessor;
        this.tradingRequestProcessor = tradingRequestProcessor;
        this.orderBookEvents = orderBookEvents;
    }

    @Override
    public void onStart(final Cluster cluster, final Image snapshotImage)
    {
        this.cluster = cluster;
        LOGGER.info("Cluster node {} started.", cluster.memberId());
        idleStrategy = cluster.idleStrategy();
        if (snapshotImage != null)
        {
            LOGGER.info("Attempting to load from snapshot.");
            loadFromSnapshot(snapshotImage);
        }
        else
        {
            bootstrap();
        }
        exchange.bindState();
    }

    @Override
    public void onTerminate(final Cluster cluster)
    {
        LOGGER.info("Cluster node {} is in onTermination. It's role is {}", cluster.memberId(), cluster.role());
        terminated.set(true);
        try
        {
            cluster.forEachClientSession(ClientSession::close);
        }
        catch (final ClusterException e)
        {
            LOGGER.error("Exception was thrown during cluster node termination. This is probably because the node was disconnected manually:", e);
        }
    }

    @Override
    public void onSessionOpen(final ClientSession session, final long timestamp)
    {
        final String principal = new String(session.encodedPrincipal(), StandardCharsets.UTF_8);
        LOGGER.info("Cluster node {} opened session principal {} at {}", cluster.memberId(), principal, timestamp);
    }

    @Override
    public void onSessionClose(final ClientSession session, final long timestamp, final CloseReason closeReason)
    {
        final String principal = new String(session.encodedPrincipal(), StandardCharsets.UTF_8);

        LOGGER.info(
            "Cluster node {} closed session {} closed at {} due to {}. Number of open client sessions: {}",
            cluster.memberId(),
            principal,
            timestamp,
            closeReason,
            cluster.clientSessions().size()
        );
    }

    @Override
    public void onSessionMessage(
        final ClientSession session,
        final long timestamp,
        final DirectBuffer buffer,
        final int offset,
        final int length,
        final Header header
    )
    {
    }

    @Override
    public void onTimerEvent(final long correlationId, final long timestamp)
    {
        LOGGER.info("Cluster node {} is in on timer event", cluster.memberId());
    }

    @Override
    public void onTakeSnapshot(final ExclusivePublication snapshotPublication)
    {
        LOGGER.info("Attempting to take snapshot...");
        Exchange.CODEC.encodeState(exchange, exchangeStateEncoder.wrap(snapshotBuffer, 0));
        final boolean snapshotSuccessful = reliableSnapshotOffer(
            snapshotPublication,
            exchangeStateEncoder.buffer(),
            exchangeStateEncoder.offset(),
            exchangeStateEncoder.buffer().capacity()
        );
        LOGGER.info("Snapshot taken successfully: {}", snapshotSuccessful);
    }

    @Override
    public void onRoleChange(final Cluster.Role newRole)
    {
        LOGGER.info("Cluster node {} has new role: {}", getMemberId(), newRole);
    }

    private void bootstrap()
    {
        exchange = Exchange.bootstrap(adminRequestProcessor, tradingRequestProcessor, orderBookEvents);
    }

    private boolean reliableSnapshotOffer(
        final ExclusivePublication snapshotPublication,
        final DirectBuffer buffer,
        final int offset,
        final int length
    )
    {
        int attempts = 3; // arbitrary
        do
        {
            final long result = snapshotPublication.offer(buffer, offset, length);
            if (result > 0)
            {
                return true;
            }
        }
        while (--attempts > 0);
        return false;
    }

    private void loadFromSnapshot(final Image snapshotImage)
    {
        final MutableBoolean allDataLoaded = new MutableBoolean(false);
        while (!snapshotImage.isEndOfStream())
        {
            final int fragmentsPolled = snapshotImage.poll((buffer, offset, length, header) -> {
                exchangeStateDecoder.wrap(buffer, offset, 0, 0);
                exchange = Exchange.CODEC.decodeState(exchangeStateDecoder);
                exchange.bindAdminRequestProcessor(adminRequestProcessor);
                exchange.bindTradingRequestProcessor(tradingRequestProcessor);
                allDataLoaded.set(true);
            }, 1);

            if (allDataLoaded.value)
            {
                break;
            }
            idleStrategy.idle(fragmentsPolled);
        }
    }

    public int getMemberId()
    {
        return cluster.memberId();
    }

    public Cluster.Role getRole()
    {
        return terminated.get() ? Cluster.Role.FOLLOWER : cluster.role();
    }
}
