package software.uncharted.terarium.hmiserver.models.xdd;

import javax.json.bind.annotation.JsonbProperty;

public class XDDSearchPayload {
	@JsonbProperty("doi")
	public String doi;

	@JsonbProperty("term")
	public String term;

	@JsonbProperty("title")
	public String title;

	@JsonbProperty("dataset") // dataset/collection name
	public String dataset;

	// Extraction-specific fields
	@JsonbProperty("type")
	public String type;

	@JsonbProperty("ignore_bytes")
	public Boolean ignore_bytes;
};
