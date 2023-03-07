package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDSearchPayload implements Serializable {
	private String doi;

	private String term;

	private String dataset; // dataset/collection name

	// Comma-separated list of dictionary names
	@JsonAlias("dict")
	private String dictNames;

	// No value required
	@JsonAlias("full_results")
	private Object fullResults;

	// Maximum number of results to include in one response.
	// Applies to full_results pagination or single-page requests
	@JsonAlias("per_page")
	private Number perPage;

	// Maximum number of articles to return
	private Number max;

	@JsonAlias("include_score")
	private Boolean includeScore;

	@JsonAlias("include_highlights")
	private Boolean includeHighlights;

	@JsonAlias("min_published")
	private String minPublished; // Date

	@JsonAlias("max_published")
	private String maxPublished; // Date

	private String pubname;

	private String publisher;

	@JsonAlias("additional_fields")
	private Boolean additionalFields;

	private Boolean match;

	// Extraction-specific field
	private String type;

	// Extraction-specific fields
	@JsonAlias("ignore_bytes")
	private Boolean ignoreBytes;
}
