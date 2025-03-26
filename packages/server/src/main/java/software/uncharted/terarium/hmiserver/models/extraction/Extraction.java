package software.uncharted.terarium.hmiserver.models.extraction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
		// Build cache for lookup
		Map<String, String> textCache = new HashMap();
		this.extractions.stream()
			.forEach(item -> {
				if (item.getType().equalsIgnoreCase("text") || item.getType().equalsIgnoreCase("table")) {
					textCache.put(item.getId(), item.getText());
				}
			});

		Map<String, List<ExtractionRef>> groupCache = new HashMap();
		this.groups.stream()
			.forEach(item -> {
				groupCache.put(item.getId(), item.getChildren());
			});

		StringBuffer buffer = new StringBuffer();
		extractTextToBuffer(buffer, textCache, groupCache, this.body.getChildren());

		return buffer.toString();
	}

	// Recursively pull out PDF text according to inferred-layout
	private static void extractTextToBuffer(
		final StringBuffer buffer,
		final Map<String, String> textCache,
		final Map<String, List<ExtractionRef>> groupCache,
		final List<ExtractionRef> children
	) {
		children
			.stream()
			.forEach(ref -> {
				final String id = ref.getId();
				final String text = textCache.get(id);
				if (text != null) {
					buffer.append(text);
					buffer.append("\n");
				} else {
					final List<ExtractionRef> childList = groupCache.get(id);
					if (childList != null) {
						extractTextToBuffer(buffer, textCache, groupCache, childList);
					}
				}
			});
	}

	// Collect simplified extraction items, used when sending for an LLM request to save tokens
	@JsonIgnore
	public List<LightweightExtractionItem> getLightweightExtractions() {
		return extractions
			.stream()
			.map(item -> {
				LightweightExtractionItem lightweight = new LightweightExtractionItem();
				lightweight.setId(item.getId());
				lightweight.setText(item.getText());
				return lightweight;
			})
			.collect(Collectors.toList());
	}
}
