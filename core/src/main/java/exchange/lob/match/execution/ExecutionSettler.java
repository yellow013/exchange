package exchange.lob.match.execution;

import org.agrona.collections.Long2LongHashMap;
import org.agrona.collections.Long2ObjectHashMap;

public interface ExecutionSettler
{
    void settle(ExecutionReport executionReport);

    static void release(
        final Long2ObjectHashMap<Long2LongHashMap> balances,
        final long userId,
        final long assetId,
        final long amount
    )
    {
        final Long2LongHashMap userBalances = balances.get(userId);
        final long assetBalance = userBalances.getOrDefault(assetId, 0L);
        userBalances.put(assetId, assetBalance + amount);
    }
}
