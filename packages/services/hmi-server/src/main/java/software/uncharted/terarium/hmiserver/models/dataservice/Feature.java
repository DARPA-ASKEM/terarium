package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Feature implements Serializable {

	private String id;

	@JsonAlias("dataset_id")
	private String datasetId;

	private String description;

	@JsonAlias("display_name")
	private String displayName;

	private String name;

	@JsonAlias("value_type")
	private ValueType valueType;
}
