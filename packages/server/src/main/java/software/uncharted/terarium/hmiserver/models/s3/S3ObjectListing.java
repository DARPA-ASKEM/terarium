package software.uncharted.terarium.hmiserver.models.s3;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@TSModel
public class S3ObjectListing {

	private boolean isTruncated;
	private List<S3Object> contents = new ArrayList<>();
}
