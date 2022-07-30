package exchange.lob.fix.transport;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class DelegatingServerSocketChannel
{
    final ServerSocketChannel serverSocketChannel;

    public DelegatingServerSocketChannel(final ServerSocketChannel serverSocketChannel)
    {
        this.serverSocketChannel = serverSocketChannel;
    }

    public SocketChannel accept() throws IOException
    {
        return serverSocketChannel.accept();
    }

    public void close() throws IOException
    {
        serverSocketChannel.close();
    }

}
