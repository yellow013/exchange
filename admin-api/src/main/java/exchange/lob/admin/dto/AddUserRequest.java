package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddUserRequest
{
    public final String username;
    public final String password;

    @JsonCreator
    public AddUserRequest(final @JsonProperty("username") String username, final @JsonProperty("password") String password)
    {
        this.username = username;
        this.password = password;
    }
}
