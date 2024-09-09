package software.uncharted.terarium.hmiserver.models.modelservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PetriNet implements Serializable {

	@JsonProperty("S")
	private List<Map<String, String>> s;

	@JsonProperty("T")
	private List<Map<String, String>> t;

	@JsonProperty("I")
	private List<Map<String, Integer>> i;

	@JsonProperty("O")
	private List<Map<String, Integer>> o;
}
