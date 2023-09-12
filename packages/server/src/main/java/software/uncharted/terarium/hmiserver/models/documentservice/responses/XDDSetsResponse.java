package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class XDDSetsResponse {

	private String description;

	@JsonProperty("available_sets")
	private List<String> availableSets;

}
