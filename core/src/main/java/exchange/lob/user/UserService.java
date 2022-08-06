package exchange.lob.user;

import exchange.lob.api.sbe.ExchangeStateDecoder;
import exchange.lob.api.sbe.ExchangeStateEncoder;
import exchange.lob.domain.ExchangeResponseCode;
import exchange.lob.node.Stateful;
import exchange.lob.node.client.response.AddUserResponse;
import exchange.lob.node.client.response.GetUsersResponse;
import exchange.lob.node.client.response.VerifyUserResponse;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.agrona.collections.MutableLong;

import java.util.Objects;
import java.util.function.LongConsumer;


public class UserService
{
    public static final long EXCHANGE_USER_ID = 0L;
    public static final String EXCHANGE_USERNAME = "EXCHANGE";
    private static final String EXCHANGE_PASSWORD = "EXCHANGE";
    public static final long INVALID_USER_ID = Long.MIN_VALUE;

    private final MutableLong currentUserId;
    private final Long2ObjectRBTreeMap<User> userIdToUserMap;
    private final Object2ObjectOpenHashMap<String, User> usernameToUserMap;

    public static final Codec CODEC = new Codec();

    public UserService(
        final MutableLong userId,
        final Long2ObjectRBTreeMap<User> userIdToUserMap,
        final Object2ObjectOpenHashMap<String, User> usernameToUserMap
    )
    {
        this.currentUserId = userId;
        this.userIdToUserMap = userIdToUserMap;
        this.usernameToUserMap = usernameToUserMap;
    }

    public static UserService create()
    {
        final MutableLong userId = new MutableLong(EXCHANGE_USER_ID);

        final Long2ObjectRBTreeMap<User> userIdToUserMap = new Long2ObjectRBTreeMap<>();
        final Object2ObjectOpenHashMap<String, User> usernameToUserMap = new Object2ObjectOpenHashMap<>();
        final User exchangeUser = new User(userId.get(), EXCHANGE_USERNAME, EXCHANGE_PASSWORD);

        userIdToUserMap.put(exchangeUser.getUserId(), exchangeUser);
        usernameToUserMap.put(EXCHANGE_USERNAME, exchangeUser);

        return new UserService(userId, userIdToUserMap, usernameToUserMap);
    }

    public AddUserResponse addUser(final String username, final String password, final LongConsumer onAddUser)
    {
        if (!usernameToUserMap.containsKey(username) && validUsername(username))
        {
            final long userId = currentUserId.incrementAndGet();
            final User user = new User(userId, username, password);
            indexUser(user);
            onAddUser.accept(userId);

            return new AddUserResponse(ExchangeResponseCode.SUCCESS, userId);
        }
        else
        {
            return new AddUserResponse(ExchangeResponseCode.INVALID_USERNAME, INVALID_USER_ID);
        }
    }

    private void indexUser(final User user)
    {
        userIdToUserMap.put(user.getUserId(), user);
        usernameToUserMap.put(user.getUsername(), user);
    }

    private boolean validUsername(String username)
    {
        return !username.isBlank();
    }

    public VerifyUserResponse verifyUser(final String username, final String password)
    {
        final User user = usernameToUserMap.get(username);
        if (user == null)
        {
            return new VerifyUserResponse(ExchangeResponseCode.INVALID_USERNAME, false, INVALID_USER_ID);
        }

        final boolean verified = user.getPassword().equals(password);
        final ExchangeResponseCode code = verified ? ExchangeResponseCode.SUCCESS : ExchangeResponseCode.INVALID_PASSWORD;
        return new VerifyUserResponse(code, verified, user.getUserId());
    }

    public User getUser(final String username)
    {
        return usernameToUserMap.get(username);
    }

    public User getUser(final long userId)
    {
        return userIdToUserMap.get(userId);
    }

    public GetUsersResponse getUsers()
    {
        return new GetUsersResponse(
            userIdToUserMap.values().stream()
                .filter(user -> user.getUserId() != EXCHANGE_USER_ID)
                .toList(),
            ExchangeResponseCode.SUCCESS
        );
    }

    @Override
    public String toString()
    {
        return "UserService{" +
            "currentUserId=" + currentUserId +
            ", userIdToUserMap=" + userIdToUserMap +
            ", usernameToUserMap=" + usernameToUserMap +
            '}';
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final UserService that = (UserService)o;
        return Objects.equals(currentUserId, that.currentUserId)
            && Objects.equals(userIdToUserMap, that.userIdToUserMap)
            && Objects.equals(usernameToUserMap, that.usernameToUserMap);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(currentUserId, userIdToUserMap, usernameToUserMap);
    }

    public static class Codec implements Stateful<UserService>
    {
        @Override
        public UserService decodeState(final ExchangeStateDecoder exchangeStateDecoder)
        {
            final MutableLong currentUserId = new MutableLong();
            final Long2ObjectRBTreeMap<User> userIdToUserMap = new Long2ObjectRBTreeMap<>();
            final Object2ObjectOpenHashMap<String, User> usernameToUserIdMap = new Object2ObjectOpenHashMap<>();

            exchangeStateDecoder.users().forEachRemaining(usersDecoder -> {
                currentUserId.set(Math.max(usersDecoder.userId(), currentUserId.get()));
                final User user = new User(usersDecoder.userId(), usersDecoder.username(), usersDecoder.password());
                userIdToUserMap.put(user.getUserId(), user);
                usernameToUserIdMap.put(user.getUsername(), user);
            });

            return new UserService(currentUserId, userIdToUserMap, usernameToUserIdMap);
        }

        @Override
        public void encodeState(final UserService userService, final ExchangeStateEncoder exchangeStateEncoder)
        {
            final Long2ObjectRBTreeMap<User> userIdToUserMap = userService.userIdToUserMap;
            final ExchangeStateEncoder.UsersEncoder usersEncoder = exchangeStateEncoder.usersCount(userIdToUserMap.size());

            userIdToUserMap.forEach((userId, user) -> usersEncoder.next()
                .userId(user.getUserId())
                .username(user.getUsername())
                .password(user.getPassword())
            );
        }
    }
}
