package exchange.lob.user;

import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.Objects;

public class User implements Encodable
{
    private final long userId;
    private final String username;
    private final String password;

    public User(final long userId, final String username, final String password)
    {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public long getUserId()
    {
        return userId;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(userId, username, password);
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
        final User user = (User)o;
        return userId == user.userId && Objects.equals(username, user.username) && Objects.equals(password, user.password);
    }

    @Override
    public String toString()
    {
        return "User{" +
            "userId=" + userId +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            '}';
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(userId);
        bufferEncoder.encode(username);
        bufferEncoder.encode(password);
    }

    @DecodedBy
    public static User decode(final BufferDecoder bufferDecoder)
    {
        final long userId = bufferDecoder.decodeLong();
        final String username = bufferDecoder.decodeString();
        final String password = bufferDecoder.decodeString();
        return new User(userId, username, password);
    }
}
