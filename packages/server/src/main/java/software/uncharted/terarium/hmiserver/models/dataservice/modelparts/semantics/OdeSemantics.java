package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class OdeSemantics implements SupportAdditionalProperties {
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
