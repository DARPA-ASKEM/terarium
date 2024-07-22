package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.DatasetLocation;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.TimeSpan;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a calibration job in simulation-service
public class CalibrationRequestCiemss implements Serializable {

	@JsonAlias("model_config_id")
	private UUID modelConfigId;

	private Object extra;

	@TSOptional
	private TimeSpan timespan;

	@TSOptional
	@JsonAlias("policy_intervention_id")
	private UUID policyInterventionId;

	private DatasetLocation dataset;
	private String engine;
}
