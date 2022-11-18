package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class Feature {
	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("dataset_id")
	public String datasetId;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("display_name")
	public String displayName;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("value_type")
	public ValueType valueType;

	public Feature(final String datasetId, final String description, final String displayName, final String name, final ValueType valueType) {
		this.datasetId = datasetId;
		this.description = description;
		this.displayName = displayName;
		this.name = name;
		this.valueType = valueType;
	}
}
