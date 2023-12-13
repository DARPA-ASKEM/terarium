package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
public class ModelConfiguration {
	private UUID id;

	private String name;

	@TSOptional
	private String description;

	@JsonAlias("model_id")
	private UUID modelId;

	private Object configuration;

}
