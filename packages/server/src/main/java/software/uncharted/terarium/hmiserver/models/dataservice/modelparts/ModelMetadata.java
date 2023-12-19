package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Card;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;

@Data
@Accessors(chain = true)
@Deprecated
public class ModelMetadata {
	@JsonAlias("processed_at")
	@TSOptional
	private Long processedAt;

	@JsonAlias("processed_by")
	@TSOptional
	private String processedBy;

	@JsonAlias("variable_statements")
	@TSOptional
	private List<VariableStatement> variableStatements;

	@JsonAlias("annotations")
	@TSOptional
	private Annotations annotations;

	@JsonAlias("attributes")
	@TSOptional
	private List<JsonNode> attributes;

	@JsonAlias("timeseries")
	@TSOptional
	private Map<String, Object> timeseries;

	@JsonAlias("card")
	@TSOptional
	private Card card;

	@JsonAlias("provenance")
	@TSOptional
	private List<String> provenance;
}
