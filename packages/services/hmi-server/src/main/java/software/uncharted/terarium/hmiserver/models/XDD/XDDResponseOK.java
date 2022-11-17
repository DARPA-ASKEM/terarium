package software.uncharted.terarium.hmiserver.models.xdd;

import javax.json.bind.annotation.JsonbProperty;

public class XDDResponseOK {
	@JsonbProperty("v")
	public Number v;

	@JsonbProperty("license")
	public String license;
};
