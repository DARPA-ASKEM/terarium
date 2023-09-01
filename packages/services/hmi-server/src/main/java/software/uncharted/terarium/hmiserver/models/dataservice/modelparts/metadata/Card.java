package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
public class Card {
    @JsonProperty("DESCRIPTION")
	@TSOptional
	private String description;

	@JsonProperty("AUTHOR_INST")
	@TSOptional
	private String authorInst;

	@JsonProperty("AUTHOR_AUTHOR")
	@TSOptional
	private String authorAuthor;

	@JsonProperty("AUTHOR_EMAIL")
	@TSOptional
	private String authorEmail;

	@JsonProperty("DATE")
    @TSOptional
	private String date;

	@JsonProperty("SCHEMA")
	@TSOptional
	private String schema;

	@JsonProperty("PROVENANCE")
	@TSOptional
	private String provenance;

	@JsonProperty("DATASET")
	@TSOptional
	private String dataset;

	@JsonProperty("COMPLEXITY")
	@TSOptional
	private String complexity;

	@JsonProperty("USAGE")
	@TSOptional
	private String usage;

	@JsonProperty("LICENSE")
	@TSOptional
	private String license;
}
