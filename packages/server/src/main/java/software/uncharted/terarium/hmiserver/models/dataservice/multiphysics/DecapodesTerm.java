package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class DecapodesTerm {
	@TSOptional
	private String name;

	@TSOptional
	private DecapodesTerm var;

	@TSOptional
	private String symbol;

	@TSOptional
	private String space;

	@TSOptional
	private List<String> fs;

	@TSOptional
	private DecapodesTerm arg;

	@TSOptional
	private String f;

	@TSOptional
	private DecapodesTerm arg1;

	@TSOptional
	private DecapodesTerm arg2;

	@TSOptional
	private List<DecapodesTerm> args;

	private String _type;
}
