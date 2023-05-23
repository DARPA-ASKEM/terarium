package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.Map;

/**
 * Represents a grounding document from TDS
 */
@Data
@Accessors(chain = true)
@TSModel
public class Grounding {

	/** Ontological identifier per DKG **/
	private Map<String, String> identifiers;

	/** (Optional) Additional context that informs the grounding **/
	@TSOptional
	private Map<String, Object> context;
}
