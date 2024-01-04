package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

@Data
@Accessors(chain = true)
public class VariableStatement {
	private String id;
	private Variable variable;
	@TSOptional
	private StatementValue value;
	@TSOptional
	private List<VariableStatementMetadata> metadata;
	@TSOptional
	private ProvenanceInfo provenance;
}

