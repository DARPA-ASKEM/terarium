package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class PresignedURL {
	private String url;
	private String method;
}
