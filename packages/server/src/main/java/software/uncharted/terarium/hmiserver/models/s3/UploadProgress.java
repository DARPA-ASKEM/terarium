package software.uncharted.terarium.hmiserver.models.s3;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@TSModel
public class UploadProgress {

	private String uploadId;
	private int percentComplete;
}
