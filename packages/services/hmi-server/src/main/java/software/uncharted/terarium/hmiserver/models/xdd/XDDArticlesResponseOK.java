package software.uncharted.terarium.hmiserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class XDDArticlesResponseOK extends XDDResponseOK implements Serializable {
	private List<Document> data;

	@JsonbProperty("next_page")
	private String nextPage;

	private String scrollId;

	private Number hits;
}
