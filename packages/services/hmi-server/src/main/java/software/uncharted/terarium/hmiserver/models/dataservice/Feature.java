package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Feature implements Serializable {

	private String id;

	@JsonbProperty("dataset_id")
	private String datasetId;

	private String description;

	@JsonbProperty("display_name")
	private String displayName;

	private String name;

	@JsonbProperty("value_type")
	private ValueType valueType;
}
