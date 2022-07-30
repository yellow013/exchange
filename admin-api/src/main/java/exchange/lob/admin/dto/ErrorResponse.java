package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse
{
    public final String error;

    @JsonCreator
    public ErrorResponse(final @JsonProperty("error") String error)
    {
        this.error = error;
    }
}
