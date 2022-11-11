package software.uncharted.terarium.mockdataservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.json.bind.annotation.JsonbProperty;

@Data
@AllArgsConstructor
public class SimulationPlan {

	@JsonbProperty("id")
	private Long id;

	@JsonbProperty("model_id")
	private Long modelId;

	@JsonbProperty("name")
	private String name;

	@JsonbProperty("description")
	private String description;

	@JsonbProperty("simulator")
	private String simulator;

	@JsonbProperty("query")
	private String query;

	@JsonbProperty("content")
	private String content;
}
