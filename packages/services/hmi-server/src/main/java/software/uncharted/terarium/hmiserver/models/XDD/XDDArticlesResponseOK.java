package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

public class XDDArticlesResponseOK extends XDDResponseOK {
	public List<Document> data;

	@JsonbProperty("next_page")
	public String nextPage;

	public String scrollId;

	public Number hits;
};
