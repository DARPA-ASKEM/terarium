package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class Qualifier {

	@JsonbProperty("id")
	public String id;

	@JsonbProperty("dataset_id")
	public String datasetId;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("value_type")
	public ValueType valueType;

	public Qualifier(final String datasetId, final String description, final String name, final ValueType valueType) {
		this.datasetId = datasetId;
		this.description = description;
		this.name = name;
		this.valueType = valueType;
	}
}
