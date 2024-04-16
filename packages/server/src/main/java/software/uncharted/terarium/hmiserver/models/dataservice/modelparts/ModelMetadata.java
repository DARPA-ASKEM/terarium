package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Card;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;

@EqualsAndHashCode(callSuper = true)
@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelMetadata extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = 1847034755264399454L;

	@TSOptional
	@JsonProperty("processed_at")
	private Long processedAt;

	@TSOptional
	@JsonProperty("processed_by")
	private String processedBy;

	@TSOptional
	@JsonProperty("variable_statements")
	private List<VariableStatement> variableStatements;

	@TSOptional
	private Annotations annotations;

	@TSOptional
	private List<JsonNode> attributes;

	@TSOptional
	private Map<String, Object> timeseries;

	@TSOptional
	private Map<String, Object> initials;

	@TSOptional
	private Map<String, Object> parameters;

	@TSOptional
	private Card card;

	@TSOptional
	@JsonProperty("gollmCard")
	private JsonNode gollmCard;

	@TSOptional
	@JsonProperty("gollmExtractions")
	private JsonNode gollmExtractions;

	@TSOptional
	private List<String> provenance;

	@TSOptional
	@JsonProperty("templateCard")
	private Object templateCard;

	@TSOptional
	@JsonProperty("code_id")
	String codeId;
}
