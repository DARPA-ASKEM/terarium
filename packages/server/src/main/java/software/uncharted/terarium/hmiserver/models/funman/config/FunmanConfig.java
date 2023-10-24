package software.uncharted.terarium.hmiserver.models.funman.config;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonInclude;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TSModel
public class FunmanConfig {
	private Double tolerance;
	private Integer queueTimeout;
	private Integer numberOfProcesses;
	private Integer waitTimeout;
	private Double waitActionTimeout;
	private String solver;
	private Integer numSteps;
	private Integer stepSize;
	private Integer numInitialBoxes;
	private Boolean saveSmtlib;
	private Double drealPrecision;
	private String drealLogLevel;
	private Double constraintNoise;
	private Double initialStateTolerance;
	private Boolean drealMcts;
	private Boolean substituteSubformulas;
	private Boolean useCompartmentalConstraints;
	private Boolean normalize;
}
