package exchange.lob.match.execution;

import exchange.lob.api.codecs.internal.Side;
import exchange.lob.domain.RejectionReason;
import exchange.lob.product.Product;
import org.agrona.collections.Long2LongHashMap;
import org.agrona.collections.Long2ObjectHashMap;

public interface ExecutionReport
{
    Product getProduct();

    long getExecutionId();

    RejectionReason getRejectionReason();

    Side getSide();

    long getPrice();

    long getAmount();

    void settle(Long2ObjectHashMap<Long2LongHashMap> balances);
}
