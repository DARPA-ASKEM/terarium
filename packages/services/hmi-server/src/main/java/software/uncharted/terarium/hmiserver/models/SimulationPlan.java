package software.uncharted.terarium.hmiserver.models;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Map;


public class SimulationPlan {

	@JsonbProperty("id")
	public Long id;

	@JsonbProperty("model_id")
	public Long modelId;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("simulator")
	public String simulator;

	@JsonbProperty("query")
	public String query;

	@JsonbProperty("content")
	public String content;
}
