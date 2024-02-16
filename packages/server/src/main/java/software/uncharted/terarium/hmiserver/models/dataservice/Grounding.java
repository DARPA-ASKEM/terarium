package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Represents a grounding document from TDS
 */
@Data
@Accessors(chain = true)
@TSModel
public class Grounding implements Serializable {

	@Serial
	private static final long serialVersionUID = 302308407252037615L;

	/**
	 * Ontological identifier per DKG
	 **/
	private Map<String, String> identifiers;

	/**
	 * (Optional) Additional context that informs the grounding
	 **/
	@TSOptional
	private Map<String, Object> context;
}
