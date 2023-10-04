package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.DatasetLocation;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.EnsembleModelConfigs;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.TimeSpan;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class EnsembleCalibrationCiemssRequest implements Serializable {
	@JsonAlias("model_configs")
	private List<EnsembleModelConfigs> modelConfigs;

	private DatasetLocation dataset;

	@JsonAlias("time_span")
	private TimeSpan timespan;
	private Object extra;

	private String engine;
}
