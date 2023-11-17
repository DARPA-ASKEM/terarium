package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Card;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@Deprecated
public class ModelMetadata {
	@JsonProperty("processed_at")
	@TSOptional
	private Long processedAt;

	@JsonProperty("processed_by")
	@TSOptional
	private String processedBy;

	@JsonProperty("variable_statements")
	@TSOptional
	private List<VariableStatement> variableStatements;

	@JsonProperty("annotations")
	@TSOptional
	private Annotations annotations;

	@JsonProperty("attributes")
	@TSOptional
	private List<JsonNode> attributes;

	@JsonProperty("timeseries")
	@TSOptional
	private Map<String, Object> timeseries;

	@JsonProperty("card")
	@TSOptional
	private Card card;

	@JsonProperty("provenance")
	@TSOptional
	private List<String> provenance;
}
