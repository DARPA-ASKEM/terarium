package software.uncharted.terarium.documentserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDUrlExtraction implements Serializable {

	private String url;

	private String resourceTitle;

	private String extractedFrom;

	@JsonbProperty("resource_title")
	public void setResourceTitle(String resourceTitle) {
		this.resourceTitle = resourceTitle;
	}

	@JsonbProperty("extracted_from")
	public void setExtractedFrom(String extractedFrom) {
		this.extractedFrom = extractedFrom;
	}

}
