package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;

@Data
@Accessors(chain = true)
public class ModelMetadata {
	@JsonProperty("processed_at")
	private Long processedAt;

	@JsonProperty("processed_by")
	private String processedBy;

	@JsonProperty("variable_statements")
	private List<VariableStatement> variableStatements;

	@JsonProperty("annotations")
	private Annotations annotations;
}
