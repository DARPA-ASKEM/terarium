package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class Variable implements SupportAdditionalProperties {
	private String id;
	private String name;
	private List<VariableMetadata> metadata;

	@JsonProperty("dkg_groundings")
	private List<DKGConcept> dkgGroundings;

	private List<DataColumn> column;

	private Paper paper;

	private List<EquationVariable> equations;
}
