package exchange.lob.fix;

import exchange.lob.api.codecs.fix.OrdType;
import exchange.lob.api.codecs.fix.decoder.NewOrderSingleDecoder;
import exchange.lob.api.codecs.fix.decoder.OrderCancelRequestDecoder;
import exchange.lob.events.trading.TradingRequests;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import static exchange.lob.fix.util.OrderTypes.toInternal;
import static exchange.lob.fix.util.Sides.toInternal;

public class ExchangeMessageDispatcher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeMessageDispatcher.class);

    private final NewOrderSingleDecoder newOrderSingle = new NewOrderSingleDecoder();
    private final OrderCancelRequestDecoder orderCancelRequest = new OrderCancelRequestDecoder();
    private final MutableAsciiBuffer asciiBuffer = new MutableAsciiBuffer();
    private final TradingRequests tradingRequests;
    private final String compId;

    public ExchangeMessageDispatcher(final TradingRequests tradingRequests, final String compId)
    {
        this.tradingRequests = tradingRequests;
        this.compId = compId;
    }


    public void dispatchToExchange(final DirectBuffer buffer, final int offset, final int length, final long messageType)
    {
        LOGGER.info("FIX Session: {} sent message of type: {}", compId, messageType);

        asciiBuffer.wrap(buffer, offset, length);

        if (messageType == NewOrderSingleDecoder.MESSAGE_TYPE)
        {
            decodeAndSendPlaceOrderRequest(length);
        }

        else if (messageType == OrderCancelRequestDecoder.MESSAGE_TYPE)
        {
            decodeAndSendCancelOrderRequest(length);
        }
    }

    private void decodeAndSendCancelOrderRequest(final int length)
    {
        orderCancelRequest.decode(asciiBuffer, 0, length);

        final String productSymbol = orderCancelRequest.symbolAsString();
        final String clientOrderId = String.valueOf(orderCancelRequest.clOrdID());
        final double amount = orderCancelRequest.orderQty().toDouble();

        tradingRequests.cancelOrder(compId, productSymbol, clientOrderId, amount);
    }

    private void decodeAndSendPlaceOrderRequest(final int length)
    {
        newOrderSingle.decode(asciiBuffer, 0, length);

        final String clientOrderId = String.valueOf(newOrderSingle.clOrdID());
        final String productSymbol = newOrderSingle.symbolAsString();
        final OrdType ordType = newOrderSingle.ordTypeAsEnum();
        final exchange.lob.api.codecs.fix.Side side = newOrderSingle.sideAsEnum();
        final double price = ordType == OrdType.MARKET ? Long.MIN_VALUE : newOrderSingle.price().toDouble();
        final double amount = newOrderSingle.orderQty().toDouble();

        tradingRequests.placeOrder(compId, productSymbol, clientOrderId, toInternal(ordType), toInternal(side), price, amount);
    }
}
