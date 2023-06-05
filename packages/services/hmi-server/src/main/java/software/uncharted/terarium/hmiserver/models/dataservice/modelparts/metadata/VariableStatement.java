package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;

@Data
@Accessors(chain = true)
public class VariableStatement {
	private String id;
	private Variable variable;
	private StatementValue value;
	private List<VariableStatementMetadata> metadata;
	private ProvenanceInfo provenance;
}

