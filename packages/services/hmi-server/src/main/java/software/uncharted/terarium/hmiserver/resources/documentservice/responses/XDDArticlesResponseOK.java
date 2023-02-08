package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

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
