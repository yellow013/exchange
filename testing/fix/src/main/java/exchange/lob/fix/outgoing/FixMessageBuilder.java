package exchange.lob.fix.outgoing;

import exchange.lob.fix.FixUtil;
import exchange.lob.fix.fields.EncryptMethod;
import exchange.lob.fix.fields.MsgType;
import exchange.lob.fix.fields.OrdType;
import exchange.lob.fix.fields.SessionRejectReason;
import exchange.lob.fix.fields.Side;
import exchange.lob.fix.fields.*;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static exchange.lob.fix.fields.Tags.*;


/**
 * FixMessageBuilder used for constructing messages to be sent.
 * Tags 8 BeginString, 9 MessageLength and 10 CheckSum are automatically appended in the correct order.
 * The message length and checksum can be overridden with the {@link #overrideMessageLength(String)}. and {@link #overrideChecksum(String)} methods.
 * All other fix tags & values are appended to the fix message in the order that they are specified.
 * Care must be taken to ensure the order of fields is valid for the version of the FIX protocol being used.
 */
public class FixMessageBuilder
{
    private static final String SOH = "\u0001";
    private final StringBuilder msg = new StringBuilder();
    private final String version;

    private String messageLengthOverride;
    private String checksumOverride;

    /**
     * Constructs a MessageBuilder for FIX messages with a version 4.4 begin string.
     */
    public FixMessageBuilder()
    {
        this("FIX.4.4");
    }

    /**
     *  Constructs a MessageBuilder with the specified begin string
     *
     * @param version The FIX Begin String (fix tag 8) to be used in the msg.
     */
    public FixMessageBuilder(final String version)
    {
        this.version = version;
    }

    /**
     * Allows overriding of the calculated message length
     * @param messageLengthOverride The value of the (9) message length tag.
     * @return FixMessageBuilder
     */
    public FixMessageBuilder overrideMessageLength(final String messageLengthOverride)
    {
        this.messageLengthOverride = messageLengthOverride;
        return this;
    }

    /**
     * Allows overriding of the calculated checksum.
     * @param checksumOverride The value of the (10) checksum tag.
     * @return FixMessageBuilder
     */
    public FixMessageBuilder overrideChecksum(final String checksumOverride)
    {
        this.checksumOverride = checksumOverride;
        return this;
    }

    public FixMessageBuilder messageType(final String type)
    {
        if (exchange.lob.fix.fields.MsgType.knownMsgType(type))
        {
            throw new RuntimeException("Please use the MsgType enumeration");
        }
        return addTag(Tags.MsgType.getTag(), type);
    }

    public FixMessageBuilder account(final String account)
    {
        return addTag(Tags.Account.getTag(), account);
    }

    public FixMessageBuilder messageType(final MsgType type)
    {
        return addTag(Tags.MsgType.getTag(), type.getCode());
    }

    public FixMessageBuilder senderCompID(final String senderCompID)
    {
        return addTag(SenderCompID.getTag(), senderCompID);
    }

    public FixMessageBuilder targetCompID(final String targetCompID)
    {
        return addTag(TargetCompID.getTag(), targetCompID);
    }

    public FixMessageBuilder beginSeqNo(final int beginSeqNo)
    {
        return addTag(BeginSeqNo.getTag(), Integer.toString(beginSeqNo));
    }

    public FixMessageBuilder endSeqNo(final int endSeqNo)
    {
        return addTag(EndSeqNo.getTag(), Integer.toString(endSeqNo));
    }

    public FixMessageBuilder refSeqNum(final int refSeqNum)
    {
        return addTag(RefSeqNum.getTag(), Integer.toString(refSeqNum));
    }

    public FixMessageBuilder testReqId(final String testReqId)
    {
        return addTag(TestReqID.getTag(), testReqId);
    }

    public FixMessageBuilder resetSeqNumFlag(final String resetSeqNumFlag)
    {
        return addTag(ResetSeqNumFlag.getTag(), resetSeqNumFlag);
    }

    public FixMessageBuilder msgSeqNum(final int msgSeqNum)
    {
        return addTag(MsgSeqNum.getTag(), Integer.toString(msgSeqNum));
    }

    public FixMessageBuilder msgSeqNum(final String msgSeqNum)
    {
        return addTag(MsgSeqNum.getTag(), msgSeqNum);
    }

    public FixMessageBuilder sendingTime(final LocalDateTime sendingTime)
    {
        return addTag(SendingTime.getTag(), FixUtil.DATE_TIME_FORMATTER.format(sendingTime));
    }

    public FixMessageBuilder sendingTimeNow()
    {
        return addTag(SendingTime.getTag(), FixUtil.DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)));
    }

    public FixMessageBuilder username(final String username)
    {
        return addTag(Username.getTag(), username);
    }

    public FixMessageBuilder password(final String password)
    {
        return addTag(Password.getTag(), password);
    }

    public FixMessageBuilder heartBtInt(final int heartbeatInterval)
    {
        return addTag(HeartBtInt.getTag(), Integer.toString(heartbeatInterval));
    }

    public FixMessageBuilder rawData(final String data)
    {
        addTag(RawDataLength.getTag(), Integer.toString(data.length()));
        return addTag(RawData.getTag(), data);
    }

    public FixMessageBuilder encryptMethod(final EncryptMethod encryptMethod)
    {
        return addTag(EncryptMethod.getTag(), encryptMethod.getCode());
    }

    public FixMessageBuilder clOrdID(final String clOrdId)
    {
        return addTag(ClOrdID.getTag(), clOrdId);
    }

    public FixMessageBuilder symbol(final String symbol)
    {
        return addTag(Symbol.getTag(), symbol);
    }

    public FixMessageBuilder side(final Side side)
    {
        return addTag(Side.getTag(), Integer.toString(side.getCode()));
    }

    public FixMessageBuilder transactionTime(final LocalDateTime transactionTime)
    {
        return addTag(TransactTime.getTag(), FixUtil.DATE_TIME_FORMATTER.format(transactionTime));
    }

    public FixMessageBuilder orderQty(final BigDecimal orderQty)
    {
        return addTag(OrderQty.getTag(), orderQty.toPlainString());
    }

    public FixMessageBuilder orderQty(final String orderQty)
    {
        return addTag(OrderQty.getTag(), orderQty);
    }

    public FixMessageBuilder orderType(final OrdType ordType)
    {
        return addTag(OrdType.getTag(), Integer.toString(ordType.getCode()));
    }

    public FixMessageBuilder price(final BigDecimal price)
    {
        return addTag(Price.getTag(), price.toPlainString());
    }

    public FixMessageBuilder price(final String price)
    {
        return addTag(Price.getTag(), price);
    }

    public FixMessageBuilder securityID(final String symbol)
    {
        return addTag(SecurityID.getTag(), symbol);
    }

    public FixMessageBuilder securityIDSource(final String symbol)
    {
        return addTag(SecurityIDSource.getTag(), symbol);
    }

    public FixMessageBuilder refMsgType(final MsgType refMsgType)
    {
        return addTag(RefMsgType.getTag(), refMsgType.getCode());
    }

    public FixMessageBuilder businessRejectReason(final BusinessRejectionReason rejectionReason)
    {
        return addTag(BusinessRejectReason.getTag(), Integer.toString(rejectionReason.getCode()));
    }

    public FixMessageBuilder sessionRejectReason(final SessionRejectReason rejectReason)
    {
        return addTag(Tags.SessionRejectReason.getTag(), Integer.toString(rejectReason.getCode()));
    }

    public FixMessageBuilder possDuplicate(final boolean possDup)
    {
        return addTag(Tags.PossDupFlag.getTag(), possDup ? "Y" : "N");
    }

    public FixMessageBuilder origSendingTime(final LocalDateTime origSendingTime)
    {
        return addTag(OrigSendingTime.getTag(), FixUtil.DATE_TIME_FORMATTER.format(origSendingTime));
    }


    public FixMessageBuilder append(final int tag, final String value)
    {
        return addTag(tag, value);
    }

    public FixMessage build()
    {
        final String body = msg.toString();
        final String messageWithHeader = "8=" + version + SOH + "9=" + messageLength(body) + SOH + body;
        final String messageWithChecksum = messageWithHeader + "10=" + checksum(messageWithHeader) + SOH;
        return new FixMessage(messageWithChecksum);
    }

    private FixMessageBuilder addTag(final int tag, final String value)
    {
        msg.append(tag);
        msg.append("=");
        msg.append(value);
        msg.append(SOH);
        return this;
    }

    private String checksum(final String bodyWithLength)
    {
        if (checksumOverride != null)
        {
            return checksumOverride;
        }

        return String.format("%03d", checksum(bodyWithLength.getBytes(Charset.forName("UTF-8"))));
    }

    private int checksum(final byte[] bytes)
    {
        int result = 0;
        for (final byte b : bytes)
        {
            result += b;
        }
        return result % 256;
    }

    private String messageLength(final String body)
    {
        if (messageLengthOverride != null)
        {
            return messageLengthOverride;
        }

        return String.valueOf(body.length());
    }
}

