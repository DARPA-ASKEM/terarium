package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.util.HashMap;
import java.util.Map;

public class SimulationPlan {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("model_id")
	public String modelId;

	@JsonbProperty("simulator")
	public String simulator;

	@JsonbProperty("query")
	public String query;

	@JsonbProperty("content")
	public String content;

	@JsonbProperty("concept")
	public Concept concept;

	@JsonbProperty("parameters")
	public Map<String, String> parameters = new HashMap<>();

	public SimulationPlan(final String modelId, final String simulator, final String query, final String content, final Concept concept, final Map<String, String> parameters) {
		this.modelId = modelId;
		this.simulator = simulator;
		this.query = query;
		this.content = content;
		this.concept = concept;
		this.parameters = parameters;
	}
}
