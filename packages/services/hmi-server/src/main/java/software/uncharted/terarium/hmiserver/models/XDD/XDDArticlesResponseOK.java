package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDArticlesResponseOK extends XDDResponseOK implements Serializable {
	private List<Document> data;

	@JsonbProperty("next_page")
	private String nextPage;

	private String scrollId;

	private Number hits;
};
