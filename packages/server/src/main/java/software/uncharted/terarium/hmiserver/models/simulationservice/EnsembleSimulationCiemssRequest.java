package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.EnsembleModelConfigs;

@Data
@Accessors(chain = true)
@TSModel
public class EnsembleSimulationCiemssRequest implements Serializable {

	@JsonAlias("model_configs")
	private List<EnsembleModelConfigs> modelConfigs;

	@JsonAlias("end_time")
	private Double endTime;

	private Object extra;

	private String engine;
}
