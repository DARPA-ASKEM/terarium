package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.ws.rs.core.Response;
import java.io.Serializable;

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
