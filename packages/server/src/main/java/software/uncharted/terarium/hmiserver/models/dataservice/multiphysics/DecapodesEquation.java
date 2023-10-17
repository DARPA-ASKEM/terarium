package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class DecapodesEquation {
	private DecapodesTerm lhs;
	private DecapodesTerm rhs;
	private String _type;
}
