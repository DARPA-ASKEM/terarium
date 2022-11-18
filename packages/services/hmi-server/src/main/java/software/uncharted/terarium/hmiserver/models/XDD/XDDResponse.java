package software.uncharted.terarium.hmiserver.models.xdd;

class XDDResponseError {
	public String message;
};

public class XDDResponse<T> {
  public T success;

  public XDDResponseError error;
};
