package software.uncharted.terarium.hmiserver.models.XDD;

import javax.json.bind.annotation.JsonbProperty;

public class XDDSearchPayload {
	@JsonbProperty("doi")
	public String doi;

	@JsonbProperty("term")
	public String term;

	@JsonbProperty("title")
	public String title;
};
