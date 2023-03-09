package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SimulationPlan implements Serializable {

	private Long id;

	@JsonProperty("model_id")
	private String modelId;

	private String simulator;

	private String query;

	private Object content;

	private Concept concept;
}
