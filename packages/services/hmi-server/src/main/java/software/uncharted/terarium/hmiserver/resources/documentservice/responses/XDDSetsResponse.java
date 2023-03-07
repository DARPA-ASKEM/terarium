package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class XDDSetsResponse {

	private String description;

	@JsonAlias("available_sets")
	private List<String> availableSets;

}
