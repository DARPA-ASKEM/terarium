package software.uncharted.terarium.mockdataservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Project {

	@JsonbProperty("id")
	private Long id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("assets")
	public Map<String, List<Long>> assets;

	@JsonbProperty("status")
	public String status;
}
