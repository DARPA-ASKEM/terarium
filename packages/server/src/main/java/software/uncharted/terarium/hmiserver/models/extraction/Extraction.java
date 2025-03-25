package software.uncharted.terarium.hmiserver.models.extraction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
		StringBuffer buffer = new StringBuffer();

		// Build cache for lookup
		Map<String, String> textCache = new HashMap();
		this.extractions.stream()
			.forEach(item -> {
				if (item.getType().equalsIgnoreCase("text") || item.getType().equalsIgnoreCase("table")) {
					textCache.put(item.getId(), item.getText());
				}
			});

		// TODO groups
		this.body.getChildren()
			.stream()
			.forEach(ref -> {
				final String id = ref.getId();
				final String text = textCache.get(id);
				if (text != null) {
					buffer.append(text);
					buffer.append("\n");
				}
			});

		return buffer.toString();
	}
}
