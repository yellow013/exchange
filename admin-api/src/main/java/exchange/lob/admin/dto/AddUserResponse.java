package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddUserResponse
{
    public final long userId;

    @JsonCreator
    public AddUserResponse(final @JsonProperty("userId") long userId)
    {
        this.userId = userId;
    }
}
