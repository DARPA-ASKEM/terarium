package software.uncharted.terarium.hmiserver.models.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Slf4j
@Data
@TSModel
public class Extraction {

	private String extractedBy;

	// Layout infomration, not used, type these out later - Mar 20
	private JsonNode pages;
	private JsonNode body;
	private JsonNode groups;

	private List<ExtractionItem> extractions;

	// Collect overall document text
	public String getDocumentText() {
		return "";
	}
}
