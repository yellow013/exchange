package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetUsersResponse
{
    public final List<User> users;

    @JsonCreator
    public GetUsersResponse(final @JsonProperty("users") List<User> users)
    {
        this.users = users;
    }

    public static class User
    {
        public final long userId;
        public final String username;

        @JsonCreator
        public User(final @JsonProperty("userId") long userId, final @JsonProperty("username") String username)
        {
            this.userId = userId;
            this.username = username;
        }

        public static User toResponse(final exchange.lob.user.User user)
        {
            return new User(user.getUserId(), user.getUsername());
        }

        @Override
        public String toString()
        {
            return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                '}';
        }
    }
}
