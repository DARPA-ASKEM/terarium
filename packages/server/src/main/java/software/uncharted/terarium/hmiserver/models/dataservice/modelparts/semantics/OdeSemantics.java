package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;

import java.util.List;

@Data
@Accessors(chain = true)
public class OdeSemantics {
	private List<Rate> rates;

	@TSOptional
	private List<Initial> initials;

	@TSOptional
	private List<ModelParameter> parameters;

	@TSOptional
	private List<Observable> observables;

	@TSOptional
	private JsonNode time;
}
