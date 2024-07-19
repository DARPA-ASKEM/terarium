package software.uncharted.terarium.hmiserver.models.funman.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanParameter {

	/** This is expected to be a model param's ID * */
	private String name;

	private FunmanInterval interval;
	/** This is currently expected to say "all" or "any" but unsure how stable this is (Nov 2023) * */
	private String label;
}
