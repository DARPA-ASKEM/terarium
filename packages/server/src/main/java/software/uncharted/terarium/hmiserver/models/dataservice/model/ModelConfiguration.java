package software.uncharted.terarium.hmiserver.models.dataservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.util.UUID;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAssetThatSupportsAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class ModelConfiguration extends TerariumAssetThatSupportsAdditionalProperties {

	@Serial
	private static final long serialVersionUID = -4109896135386019667L;

	@TSOptional
	private String description;

	@JsonProperty("model_id")
	private UUID modelId;

	private Model configuration;

	@TSOptional
	private List<Intervention> interventions;
}
