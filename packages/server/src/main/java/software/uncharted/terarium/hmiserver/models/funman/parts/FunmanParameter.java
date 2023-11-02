package software.uncharted.terarium.hmiserver.models.funman.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanParameter {
	private String name; //This is expected to be a model param's ID
	private FunmanInterval interval;
	private String label; //This is currently expected to say "all" or "any"
}