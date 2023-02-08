package software.uncharted.terarium.hmiserver.models.documentservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class KnownEntities implements Serializable {

	public List<XDDUrlExtraction> urlExtractions;

	public Map<String, Map<String, String>> summaries;

	@JsonbProperty("url_extractions")
	public void setURLExtractions(List<XDDUrlExtraction> urlExtractions) {
		this.urlExtractions = urlExtractions;
	}

}
