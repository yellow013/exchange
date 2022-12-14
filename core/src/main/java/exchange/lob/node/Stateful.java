package exchange.lob.node;

import exchange.lob.Exchange;
import exchange.lob.api.sbe.ExchangeStateDecoder;
import exchange.lob.api.sbe.ExchangeStateEncoder;

/**
 * Interface to be implemented by stateful components of {@link Exchange}
 * in order to provide snapshot functionality to {@link ExchangeClusteredService}.
 *
 * @param <T> Stateful component type
 */
public interface Stateful<T>
{

    T decodeState(ExchangeStateDecoder exchangeStateDecoder);

    void encodeState(T state, ExchangeStateEncoder exchangeStateEncoder);
}
