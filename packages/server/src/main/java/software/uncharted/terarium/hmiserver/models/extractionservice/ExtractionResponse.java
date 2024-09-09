package software.uncharted.terarium.hmiserver.models.extractionservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ExtractionResponse {

	private String id;
	private String status;
	private ExtractionResponseResult result;
}
