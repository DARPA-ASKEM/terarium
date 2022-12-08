package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SimulationPlan implements Serializable {

	private String id;

	@JsonbProperty("model_id")
	private String modelId;

	private String simulator;

	private String query;

	private String content;

	private Concept concept;
}
