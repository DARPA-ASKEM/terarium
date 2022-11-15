package software.uncharted.terarium.hmiserver.models.XDD;

import javax.json.bind.annotation.JsonbProperty;

public class XDDExtractionsResponseOK extends XDDResponseOK {
	@JsonbProperty("data")
	public Extraction[] data;

	@JsonbProperty("total")
	public Number total;

	@JsonbProperty("page")
	public Number page;
};
