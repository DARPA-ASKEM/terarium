package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class KnownEntities implements Serializable {

	@JsonAlias("url_extractions")
	private List<XDDUrlExtraction> urlExtractions;

	@JsonAlias("askem_object")
	private List<Extraction> askemObjects;

	private List<String> summaries;


}
