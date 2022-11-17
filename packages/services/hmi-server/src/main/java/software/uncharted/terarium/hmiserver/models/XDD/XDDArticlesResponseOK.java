package software.uncharted.terarium.hmiserver.models.xdd;

import javax.json.bind.annotation.JsonbProperty;

public class XDDArticlesResponseOK extends XDDResponseOK {
	@JsonbProperty("data")
	public Document[] data;

	@JsonbProperty("next_page")
	public String next_page;

	@JsonbProperty("scrollId")
	public String scrollId;

	@JsonbProperty("hits")
	public Number hits;
};
