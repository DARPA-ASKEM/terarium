package software.uncharted.terarium.hmiserver.models.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Slf4j
@Data
public class ExtractionItem {

	// Identifying fields:
	private String id;
	private String type;
	private String subType;
	private String extractedBy;

	// Provenance fields
	private Long page;
	private Double pageWidth;
	private Double pageHeight;
	private BBox bbox;
	private List<Long> charspan;

	// Data fiels
	private String rawText;
	private String text;
	private JsonNode data;
}
