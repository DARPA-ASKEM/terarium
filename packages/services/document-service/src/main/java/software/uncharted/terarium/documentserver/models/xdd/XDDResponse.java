package software.uncharted.terarium.documentserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
class XDDResponseError implements Serializable {
	private String message;
}

@Data
@Accessors(chain = true)
public class XDDResponse<T> implements Serializable {
	private T success;

	private XDDResponseError error;
}
