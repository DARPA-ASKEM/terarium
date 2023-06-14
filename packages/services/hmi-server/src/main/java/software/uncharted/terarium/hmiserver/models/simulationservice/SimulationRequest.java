package software.uncharted.terarium.hmiserver.models.simulationservice;

import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class SimulationRequest implements Serializable {
	@JsonAlias("model_config_id")
	private String modelConfigId;

	@JsonAlias("time_span")
	private TimeSpan timespan;
	private Object extra;

	private String engine;
}
