package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class ModelFramework {

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("version")
	public String version;

	@JsonbProperty("semantics")
	public String semantics;

	public ModelFramework(final String name, final String version, final String semantics) {
		this.name = name;
		this.version = version;
		this.semantics = semantics;
	}
}
