package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;

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

	@JsonProperty("description")
	@TSOptional
	private String description;

	@JsonProperty("author_inst")
	@TSOptional
	private String authorInst;

	@JsonProperty("author_author")
	@TSOptional
	private String authorAuthor;

	@JsonProperty("author_email")
	@TSOptional
	private String authorEmail;

	@JsonProperty("date")
	@TSOptional
	private String date;

	@JsonProperty("schema")
	@TSOptional
	private String schema;

	@JsonProperty("provenance")
	@TSOptional
	private String provenance;

	@JsonProperty("dataset")
	@TSOptional
	private String dataset;

	@JsonProperty("complexity")
	@TSOptional
	private String complexity;

	@JsonProperty("usage")
	@TSOptional
	private String usage;

	@JsonProperty("license")
	@TSOptional
	private String license;
}
