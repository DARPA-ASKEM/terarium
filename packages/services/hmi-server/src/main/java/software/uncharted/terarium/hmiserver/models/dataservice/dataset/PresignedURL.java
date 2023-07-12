package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class PresignedURL {
	public String url;
	public String method;
}
