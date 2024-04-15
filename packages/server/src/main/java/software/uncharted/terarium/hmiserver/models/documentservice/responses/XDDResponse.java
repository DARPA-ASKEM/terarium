package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XDDResponse<T> implements Serializable {
    private T success;

    private XDDResponseError error;

    public String getErrorMessage() {

        if (getError() != null && !getError().getMessage().isEmpty()) {
            return getError().getMessage();
        }

        return null;
    }
}
