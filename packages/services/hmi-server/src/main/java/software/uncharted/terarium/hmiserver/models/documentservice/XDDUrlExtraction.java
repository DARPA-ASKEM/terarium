package software.uncharted.terarium.hmiserver.models.documentservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class XDDUrlExtraction implements Serializable {

	private String url;

	private String resourceTitle;

	private List<String> extractedFrom;

	@JsonbProperty("resource_title")
	public void setResourceTitle(String resourceTitle) {
		this.resourceTitle = resourceTitle;
	}

	@JsonbProperty("extracted_from")
	public void setExtractedFrom(List<String> extractedFrom) {
		this.extractedFrom = extractedFrom;
	}

}
