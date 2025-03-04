package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.EnsembleModelConfigs;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.TimeSpan;

@Data
@Accessors(chain = true)
@TSModel
public class EnsembleSimulationCiemssRequest implements Serializable {

	@JsonAlias("model_configs")
	private List<EnsembleModelConfigs> modelConfigs;

	@JsonAlias("time_span")
	private TimeSpan timespan;

	@TSOptional
	@JsonAlias("logging_step_size")
	private Double loggingStepSize;

	private Object extra;

	private String engine;
}
