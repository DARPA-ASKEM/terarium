package software.uncharted.terarium.hmiserver.models.xdd;

import javax.json.bind.annotation.JsonbProperty;

public class XDDExtractionsResponseOK extends XDDResponseOK {
	@JsonbProperty("data")
	public Extraction[] data;

	@JsonbProperty("total")
	public Number total;

	@JsonbProperty("page")
	public Number page;
};
