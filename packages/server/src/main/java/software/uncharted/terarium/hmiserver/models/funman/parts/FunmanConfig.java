package software.uncharted.terarium.hmiserver.models.funman.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanConfig {

	@TSOptional
	private Double tolerance;

	@TSOptional
	private Integer queueTimeout;

	@TSOptional
	private Integer numberOfProcesses;

	@TSOptional
	private Integer waitTimeout;

	@TSOptional
	private Double waitActionTimeout;

	@TSOptional
	private String solver;

	@TSOptional
	private Integer numSteps;

	@TSOptional
	private Integer stepSize;

	@TSOptional
	private Integer numInitialBoxes;

	@TSOptional
	private Boolean saveSmtlib;

	@TSOptional
	private Double drealPrecision;

	@TSOptional
	private String drealLogLevel;

	@TSOptional
	private Double constraintNoise;

	@TSOptional
	private Double initialStateTolerance;

	@TSOptional
	private Boolean drealMcts;

	@TSOptional
	private Boolean substituteSubformulas;

	@TSOptional
	private Boolean use_compartmental_constraints;

	@TSOptional
	private Boolean normalize;

	@TSOptional
	private Integer normalization_constant;

	@TSOptional
	private Integer verbosity;
}
