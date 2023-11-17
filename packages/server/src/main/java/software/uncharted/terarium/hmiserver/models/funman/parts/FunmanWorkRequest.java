package software.uncharted.terarium.hmiserver.models.funman.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanWorkRequest {
	@TSOptional
	private JsonNode query;
	@TSOptional
	private JsonNode constraints;
	@TSOptional
	private List<FunmanParameter> parameters;
	@TSOptional
	private FunmanConfig config;
	@TSOptional
	private JsonNode structure_parameters;
}