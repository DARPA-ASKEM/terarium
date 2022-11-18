package software.uncharted.terarium.hmiserver.models.xdd;

import javax.json.bind.annotation.JsonbProperty;

public class XDDSearchPayload {
	public String doi;

	public String term;

	public String title;

	public String dataset; // dataset/collection name

	// Extraction-specific fields
	public String type;

	@JsonbProperty("ignore_bytes")
	public Boolean ignoreBytes;
};
