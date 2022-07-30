package exchange.lob.fix.incoming;


interface MessageParserCallbackTestFactory
{
    void onMessage(final byte[] buffer, final int offset, final int length);

}
