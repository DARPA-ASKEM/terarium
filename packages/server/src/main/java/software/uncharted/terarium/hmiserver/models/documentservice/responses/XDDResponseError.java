package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XDDResponseError implements Serializable {
	private String message;
}
