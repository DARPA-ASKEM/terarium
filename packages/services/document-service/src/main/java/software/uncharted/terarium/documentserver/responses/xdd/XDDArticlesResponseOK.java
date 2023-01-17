package software.uncharted.terarium.documentserver.responses.xdd;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.documentserver.models.xdd.Document;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class XDDArticlesResponseOK extends XDDResponseOK implements Serializable {
	private List<Document> data;

	@JsonbProperty("next_page")
	private String nextPage;

	private String scrollId;

	private Number hits;

	private Map<String, XDDFacetsItemResponse> facets;
}
