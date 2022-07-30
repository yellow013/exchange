package exchange.lob.fix;


import exchange.lob.fix.outgoing.FixMessage;
import exchange.lob.fix.outgoing.OutboundMessageHandler;

import java.util.Collection;

public class FixSession
{
    private int outboundSequenceNumber;
    private OutboundMessageHandler outboundMessageSender;

    public FixSession(final OutboundMessageHandler outboundMessageSender)
    {
        this.outboundMessageSender = outboundMessageSender;
        this.outboundSequenceNumber = 1;
    }

    public void send(final Collection<FixMessage> messages)
    {
        outboundMessageSender.send(messages);
    }

    public void send(final FixMessage message)
    {
        outboundMessageSender.send(message);
    }

    public void send(final String message)
    {
        outboundMessageSender.send(message);
    }

    public void send(final byte[] bytes)
    {
        outboundMessageSender.sendBytes(bytes);
    }
}
