package software.uncharted.terarium.hmiserver.models.simulationservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SimulationParams implements Serializable {
	private String model;
	private Map<String, Double> initials;
	private List<Double> tspan;
	private Map<String, Double> params;
}
