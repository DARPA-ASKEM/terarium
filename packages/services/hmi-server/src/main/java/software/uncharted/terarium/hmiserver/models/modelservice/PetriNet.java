package software.uncharted.terarium.hmiserver.models.modelservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.json.bind.annotation.JsonbProperty;

@Data
@Accessors(chain = true)
public class PetriNet implements Serializable {
	@JsonbProperty("S")
	private List<Map<String, String>> s;

	@JsonbProperty("T")
	private List<Map<String, String>> t;

	@JsonbProperty("I")
	private List<Map<String, Integer>> i;

	@JsonbProperty("O")
	private List<Map<String, Integer>> o;
}
