package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SimulationPlan extends ResourceType implements Serializable {

	@JsonAlias("model_id")
	private String modelId;

	private String simulator;

	private String query;

	private String content;

	private Concept concept;
}
