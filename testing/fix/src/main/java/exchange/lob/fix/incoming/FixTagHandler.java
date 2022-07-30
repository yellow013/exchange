package exchange.lob.fix.incoming;

public interface FixTagHandler
{
    void messageStart();

    void onTag(int tagIdentity, byte[] message, int tagValueOffset, int tagValueLength);

    boolean isFinished();

    void messageEnd();
}
