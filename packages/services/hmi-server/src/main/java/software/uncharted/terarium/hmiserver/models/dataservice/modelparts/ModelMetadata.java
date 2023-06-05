package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;

@Data
@Accessors(chain = true)
public class ModelMetadata {
	@JsonAlias("processed_at")
	private Long processedAt;

	@JsonAlias("processed_by")
	private String processedBy;

	@JsonAlias("variable_statements")
	private List<VariableStatement> variableStatements;
}
