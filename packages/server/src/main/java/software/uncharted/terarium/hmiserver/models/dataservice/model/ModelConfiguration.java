package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class ModelConfiguration extends TerariumAsset implements SupportAdditionalProperties {

	private String name;

	@TSOptional
	private String description;

	@JsonProperty("model_id")
	private UUID modelId;

	private Object configuration;

}
