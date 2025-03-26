package software.uncharted.terarium.hmiserver.models.extraction;

import lombok.Data;

// A lightweight extraction item that can be used to send the the LLM without sending unnecessary data
@Data
public class LightweightExtractionItem {

	String id;
	String text;
}
