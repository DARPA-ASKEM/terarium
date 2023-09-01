package software.uncharted.terarium.hmiserver.model.dataservice.modelparts.semantics;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;
import software.uncharted.terarium.hmiserver.model.dataservice.modelparts.ModelParameter;

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
