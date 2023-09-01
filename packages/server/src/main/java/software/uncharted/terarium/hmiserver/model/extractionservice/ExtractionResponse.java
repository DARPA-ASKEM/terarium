package software.uncharted.terarium.hmiserver.model.extractionservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.Date;

@Data
@Accessors(chain = true)
@TSModel
public class ExtractionResponse {
		private String id;
		private Date created_at;
		private Date enqueued_at;
		private Date started_at;
		private String status;
		private String extraction_error;
		private Object result;
}
