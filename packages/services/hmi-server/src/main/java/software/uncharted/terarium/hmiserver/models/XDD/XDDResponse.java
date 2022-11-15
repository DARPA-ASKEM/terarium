package software.uncharted.terarium.hmiserver.models.XDD;

import javax.json.bind.annotation.JsonbProperty;

class XDDResponseError {
	@JsonbProperty("message")
		public String message;
	};

public class XDDResponse<T> {
	@JsonbProperty("success")
  public T success;

	@JsonbProperty("error")
  public XDDResponseError error;
};
