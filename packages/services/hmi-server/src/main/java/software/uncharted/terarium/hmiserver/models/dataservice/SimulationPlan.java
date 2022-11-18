package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;


public class SimulationPlan {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("model_id")
	public String modelId;

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

	public SimulationPlan(final String modelId, final String name, final String description, final String simulator, final String query, final String content) {
		this.modelId = modelId;
		this.name = name;
		this.description = description;
		this.simulator = simulator;
		this.query = query;
		this.content = content;
	}
}
