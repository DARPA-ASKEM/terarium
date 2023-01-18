package software.uncharted.terarium.documentserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class KnownEntities implements Serializable {

	public List<XDDUrlExtraction> urlExtractions;

	@JsonbProperty("url_extractions")
	public void setKnownEntities(List<XDDUrlExtraction> urlExtractions) {
		this.urlExtractions = urlExtractions;
	}

}
