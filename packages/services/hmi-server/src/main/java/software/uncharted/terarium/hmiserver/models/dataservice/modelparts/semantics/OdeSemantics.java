package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonAlias;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;

@Data
@Accessors(chain = true)
public class OdeSemantics {
	private List<Rate> rates;
	@TSOptional
	private List<Object> initials;

	@TSOptional
	private List<ModelParameter> parameters;

	@TSOptional
	private List<Observable> observables;
}
