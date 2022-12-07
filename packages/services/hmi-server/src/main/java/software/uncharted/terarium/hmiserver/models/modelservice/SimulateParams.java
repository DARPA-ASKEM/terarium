package software.uncharted.terarium.hmiserver.models.modelservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SimulateParams implements Serializable {
	private Map<String, Double> variables;
	private Map<String, Double> parameters;
}
