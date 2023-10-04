package software.uncharted.terarium.hmiserver.models.dataservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;


@Data
@Accessors(chain = true)
@TSModel
public class ModelConfiguration {
	private String id;

	private String name;

	@TSOptional
	private String description;

	@JsonAlias("model_id")
	private String modelId;

	private Object configuration;

}
