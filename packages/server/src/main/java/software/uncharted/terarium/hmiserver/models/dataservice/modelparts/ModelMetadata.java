package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Lob;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
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
	private JsonNode templateCard;

	@TSOptional
	@JsonProperty("code_id")
	String codeId;

	@TSOptional
	JsonNode source;

	@TSOptional
	@Lob
	@JdbcTypeCode(Types.BINARY)
	private byte[] description;

	@Override
	public ModelMetadata clone() {
		final ModelMetadata clone = (ModelMetadata) super.clone();

		clone.processedBy = this.processedBy;

		if (this.variableStatements != null) {
			clone.variableStatements = new ArrayList<>();
			for (VariableStatement variableStatement : this.variableStatements) {
				clone.variableStatements.add(variableStatement.clone());
			}
		}

		if (this.annotations != null) {
			clone.annotations = this.annotations.clone();
		}

		if (this.attributes != null) {
			clone.attributes = new ArrayList<>();
			for (JsonNode attribute : this.attributes) {
				clone.attributes.add(attribute.deepCopy());
			}
		}

		if (this.initials != null) {
			clone.initials = new HashMap<>();
			clone.initials.putAll(this.initials);
		}

		if (this.parameters != null) {
			clone.parameters = new HashMap<>();
			clone.parameters.putAll(this.parameters);
		}

		if (clone.card != null) {
			clone.card = this.card.clone();
		}

		if (gollmCard != null) {
			clone.gollmCard = this.gollmCard.deepCopy();
		}

		if (gollmExtractions != null) {
			clone.gollmExtractions = this.gollmExtractions.deepCopy();
		}

		if (provenance != null) {
			clone.provenance = new ArrayList<>();
			clone.provenance.addAll(provenance);
		}

		if (templateCard != null) {
			clone.templateCard = this.templateCard.deepCopy();
		}

		clone.codeId = this.codeId;

		if (source != null) {
			clone.source = this.source.deepCopy();
		}

		return clone;
	}
}
