package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonAlias;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;
import java.util.List;

@Data
@Accessors(chain = true)
public class OdeSemantics {
	private List<Rate> rates;
	@TSOptional
	private Object initials;

	@TSOptional
	private ModelParameter parameters;
}
