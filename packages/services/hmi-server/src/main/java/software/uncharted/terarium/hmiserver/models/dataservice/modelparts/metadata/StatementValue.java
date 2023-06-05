package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;


import java.util.List;

@Data
@Accessors(chain = true)
public class StatementValue {
	private String value;
	private String type;

	@JsonAlias("dkg_grounding")
	@JsonSetter("dkg_grounding")
	private DKGConcept dkgGrounding;
}

