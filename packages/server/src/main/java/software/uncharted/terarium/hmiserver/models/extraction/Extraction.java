package software.uncharted.terarium.hmiserver.models.extraction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.stream.Collectors;
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
	@JsonIgnore
	public String getDocumentText() {
		return "";
	}

	@Data
	public class SimpleExtractionItem {

		private String id;
		private String rawText;
	}

	// Collect simplified extraction items, used when sending for an LLM request to save tokens
	@JsonIgnore
	public List<SimpleExtractionItem> getSimplifiedExtractions() {
		return extractions
			.stream()
			.map(item -> {
				SimpleExtractionItem simplified = new SimpleExtractionItem();
				simplified.setId(item.getId());
				simplified.setRawText(item.getRawText());
				return simplified;
			})
			.collect(Collectors.toList());
	}
}
