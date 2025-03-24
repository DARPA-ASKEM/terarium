package software.uncharted.terarium.hmiserver.models.dataservice.enrichment;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class Enrichment {

	private UUID id;
	private String label;
	private EnrichmentType type;
	private JsonNode content;
	private UUID extractionAssetId;
	private String[] extractionItemIds;
	private boolean included;
}
