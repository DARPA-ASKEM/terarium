package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class KnownEntities implements Serializable {

	@JsonAlias("url_extractions")
	private List<XDDUrlExtraction> urlExtractions;

	@JsonAlias("askem_object")
	private List<Extraction> askemObjects;

	private List<JsonNode> summaries;
}
