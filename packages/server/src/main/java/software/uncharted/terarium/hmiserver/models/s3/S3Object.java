package software.uncharted.terarium.hmiserver.models.s3;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@TSModel
public class S3Object {

	private String key;

	@TSOptional
	private long lastModifiedMillis;

	@TSOptional
	private String eTag;

	@TSOptional
	private long sizeInBytes;
}
