package software.uncharted.terarium.hmiserver.models.simulationservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a calibration job in simulation-service
public class CalibrationRequestJulia implements Serializable {
	@JsonAlias("model_config_id")
	private String modelConfigId;
	private Object extra;

	@TSOptional
	private TimeSpan timespan;
	
	private DatasetLocation dataset;
	private String engine;
}
