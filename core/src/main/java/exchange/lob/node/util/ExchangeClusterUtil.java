package exchange.lob.node.util;

import io.aeron.ChannelUriStringBuilder;

import java.util.List;

public class ExchangeClusterUtil
{

    public static final String LOCALHOST = "localhost";
    private static final int PORTS_PER_NODE = 100;
    public static final int ARCHIVE_CONTROL_REQUEST_PORT_OFFSET = 1;
    public static final int ARCHIVE_CONTROL_RESPONSE_PORT_OFFSET = 2;
    private static final int CLIENT_FACING_PORT_OFFSET = 3;
    private static final int MEMBER_FACING_PORT_OFFSET = 4;
    public static final int LOG_PORT_OFFSET = 5;
    private static final int TRANSFER_PORT_OFFSET = 6;
    public static final int LOG_CONTROL_PORT_OFFSET = 7;

    public static String udpChannel(final int nodeId, final String hostname, final int basePort, final int portOffset)
    {
        final int port = calculatePort(basePort, nodeId, portOffset);
        return new ChannelUriStringBuilder()
            .media("udp")
            .termLength(64 * 1024)
            .endpoint(hostname + ":" + port)
            .build();
    }

    public static String logControlChannel(final int nodeId, final String hostname, final int basePort, final int portOffset)
    {
        final int port = calculatePort(basePort, nodeId, portOffset);
        return new ChannelUriStringBuilder()
            .media("udp")
            .termLength(64 * 1024)
            .controlMode("manual")
            .controlEndpoint(hostname + ":" + port)
            .build();
    }

    public static String ingressEndpoints(final int basePort, final List<String> hostnames)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hostnames.size(); i++)
        {
            sb.append(i).append('=');
            sb.append(hostnames.get(i)).append(':').append(calculatePort(basePort, i, CLIENT_FACING_PORT_OFFSET));
            sb.append(',');
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String clusterMembers(final int basePort, final List<String> hostnames)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hostnames.size(); i++)
        {
            sb.append(i);
            sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(basePort, i, CLIENT_FACING_PORT_OFFSET));
            sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(basePort, i, MEMBER_FACING_PORT_OFFSET));
            sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(basePort, i, LOG_PORT_OFFSET));
            sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(basePort, i, TRANSFER_PORT_OFFSET));
            sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(basePort, i, ARCHIVE_CONTROL_REQUEST_PORT_OFFSET));
            sb.append('|');
        }

        return sb.toString();
    }

    private static int calculatePort(final int basePort, final int nodeId, final int offset)
    {
        return basePort + (nodeId * PORTS_PER_NODE) + offset;
    }

}
