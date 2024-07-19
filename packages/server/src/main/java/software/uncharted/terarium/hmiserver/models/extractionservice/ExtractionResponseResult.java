package software.uncharted.terarium.hmiserver.models.extractionservice;

import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ExtractionResponseResult {

	private Date created_at;
	private Date enqueued_at;
	private Date started_at;
	private String job_error;
	private Object job_result;
}
