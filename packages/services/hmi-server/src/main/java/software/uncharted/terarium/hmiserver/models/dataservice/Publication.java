package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class Publication {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("xdd_uri")
	public String xddUri;

	public Publication(final String xddUri) {
		this.xddUri = xddUri;
	}
}
