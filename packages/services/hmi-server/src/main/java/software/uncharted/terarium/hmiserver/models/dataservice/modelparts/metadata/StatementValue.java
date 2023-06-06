package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;


import java.util.List;

@Data
@Accessors(chain = true)
public class StatementValue {
	private String value;
	private String type;

	@JsonProperty("dkg_grounding")
	@TSOptional
	private DKGConcept dkgGrounding;
}

