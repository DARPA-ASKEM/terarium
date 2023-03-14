package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DocumentsResponseOK extends XDDResponseOK implements Serializable {
	private List<Document> data;

	@JsonAlias("next_page")
	private String nextPage;

	private String scrollId;

	private Number hits;

	private Map<String, XDDFacetsItemResponse> facets;
}
