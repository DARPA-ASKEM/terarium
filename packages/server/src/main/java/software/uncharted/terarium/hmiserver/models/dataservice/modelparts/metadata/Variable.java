package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class Variable extends SupportAdditionalProperties {
	private String id;
	private String name;
	private List<VariableMetadata> metadata;

	private List<DKGConcept> dkg_groundings;

	private List<DataColumn> column;

	private Paper paper;

	private List<EquationVariable> equations;
}
