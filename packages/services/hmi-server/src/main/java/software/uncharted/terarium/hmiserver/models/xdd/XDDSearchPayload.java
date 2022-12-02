package software.uncharted.terarium.hmiserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDSearchPayload implements Serializable {
	private String doi;

	private String term;

	private String title;

	private String dataset; // dataset/collection name

	// Comma-separated list of dictionary names
	@JsonbProperty("dict")
	private String dictNames;

	// No value required
	@JsonbProperty("full_results")
	private Object fullResults;

	// Maximum number of results to include in one response.
	// Applies to full_results pagination or single-page requests
	@JsonbProperty("per_page")
	private Number perPage;

	// Maximum number of articles to return
	private Number max;

	@JsonbProperty("include_score")
	private Boolean includeScore;

	@JsonbProperty("include_highlights")
	private Boolean includeHighlights;

	@JsonbProperty("min_published")
	private String minPublished; // Date

	@JsonbProperty("max_published")
	private String maxPublished; // Date

	private String pubname;

	private String publisher;

	// Extraction-specific field
	private String type;

	// Extraction-specific fields
	@JsonbProperty("ignore_bytes")
	private Boolean ignoreBytes;
};
