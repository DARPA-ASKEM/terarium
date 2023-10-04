package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

@Data
@Accessors(chain = true)
public class RegNetEdge {
	private String source;
	private String target;
	private String id;
	private Boolean sign;

	// @TSOptional
	private RegNetBaseProperties properties;
}
