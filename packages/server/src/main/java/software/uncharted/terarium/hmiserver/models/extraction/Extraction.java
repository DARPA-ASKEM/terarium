package software.uncharted.terarium.hmiserver.models.extraction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class Extraction {

	private String extractedBy;

	// Layout infomration, not used, type these out later - Mar 20
	// private JsonNode pages;
	private Map<String, ExtractionPage> pages;
	private ExtractionBody body;
	private List<ExtractionGroup> groups;

	private List<ExtractionItem> extractions;

	// Collect overall document text
	@JsonIgnore
	public String getDocumentText() {
		return "";
	}
}
