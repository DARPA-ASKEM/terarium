package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Card;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;

@Data
@Accessors(chain = true)
@Deprecated
public class ModelMetadata extends SupportAdditionalProperties {
	@TSOptional
	private Long processedAt;

	@TSOptional
	private String processedBy;

	@TSOptional
	private List<VariableStatement> variableStatements;

	@TSOptional
	private Annotations annotations;

	@TSOptional
	private List<JsonNode> attributes;

	@TSOptional
	private Map<String, Object> timeseries;

	@TSOptional
	private Card card;

	@TSOptional
	private List<String> provenance;
}
