package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import lombok.Data;
import java.util.List;
import java.util.UUID;

import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.DatasetLocation;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;
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
	private List<Intervention> interventions;

	private DatasetLocation dataset;
	private String engine;

}
