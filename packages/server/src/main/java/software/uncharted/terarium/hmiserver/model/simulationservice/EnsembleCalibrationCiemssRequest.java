package software.uncharted.terarium.hmiserver.model.simulationservice;

import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

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
