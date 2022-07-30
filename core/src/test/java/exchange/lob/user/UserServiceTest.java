package exchange.lob.user;

import exchange.lob.api.codecs.internal.ExchangeStateDecoder;
import exchange.lob.api.codecs.internal.ExchangeStateEncoder;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.LongConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceTest
{

    private UserService userService;
    private final LongConsumer onAddUser = l -> {};

    @BeforeEach
    public void setUp()
    {
        userService = UserService.create();
    }

    @Test
    public void shouldEncodeAndDecodeEmptySnapshot()
    {
        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);

        UserService.CODEC.encodeState(userService, exchangeStateEncoder);
        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder().wrap(buffer, 0, 0, 0);
        UserService decodedUserService = UserService.CODEC.decodeState(exchangeStateDecoder);

        assertEquals(userService, decodedUserService);
    }

    @Test
    public void shouldEncodeAndDecodeNonEmptySnapshot()
    {
        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);

        userService.addUser("TEST-COMP-ID-1", "password", onAddUser);
        userService.addUser("TEST-COMP-ID-2", "password", onAddUser);
        userService.addUser("TEST-COMP-ID-3", "password", onAddUser);
        UserService.CODEC.encodeState(userService, exchangeStateEncoder);

        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder().wrap(buffer, 0, 0, 0);
        UserService decodedUserService = UserService.CODEC.decodeState(exchangeStateDecoder);

        assertEquals(userService, decodedUserService);
    }
}