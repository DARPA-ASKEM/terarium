package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class VariableStatement extends SupportAdditionalProperties {
	private String id;
	private Variable variable;
	@TSOptional
	private StatementValue value;
	@TSOptional
	private List<VariableStatementMetadata> metadata;
	@TSOptional
	private ProvenanceInfo provenance;
}
