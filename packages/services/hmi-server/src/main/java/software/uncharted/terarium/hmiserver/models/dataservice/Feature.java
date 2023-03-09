package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Feature implements Serializable {

	private String id;

	@JsonProperty("dataset_id")
	private String datasetId;

	private String description;

	@JsonProperty("display_name")
	private String displayName;

	private String name;

	@JsonProperty("value_type")
	private ValueType valueType;
}
