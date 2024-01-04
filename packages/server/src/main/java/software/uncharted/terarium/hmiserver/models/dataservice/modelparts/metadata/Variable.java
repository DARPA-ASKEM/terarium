package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Variable {
	private String id;
	private String name;
	private List<VariableMetadata> metadata;

	@JsonProperty("dkg_groundings")
	private List<DKGConcept> dkgGroundings;

	private List<DataColumn> column;

	private Paper paper;

	private List<EquationVariable> equations;
}

