package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;

import org.jose4j.json.internal.json_simple.JSONObject;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.Observables;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;

@Data
@Accessors(chain = true)
public class OdeSemantics {
	private List<Rate> rates;

	@TSOptional
	private List<Object> initials;

	@TSOptional
	private List<ModelParameter> parameters;

	@TSOptional
	private List<Observables> observables;

	@TSOptional
	private JSONObject time;
}
