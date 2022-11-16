package software.uncharted.terarium.mockdataservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Map;

@Data
@AllArgsConstructor
public class Model {

	@JsonbProperty("id")
	public Long id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("content")
	public String content;

	@JsonbProperty("parameters")
	public Map<String, String> parameters;
}
