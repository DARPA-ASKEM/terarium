package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum SimulationType {
	@JsonAlias({ "ensemble", "ensemble-simulate", "ensemble-simulation", "ensemble-calibrate", "ensemble-calibration" })
	ENSEMBLE,
	@JsonAlias({ "simulate", "simulation" })
	SIMULATION,
	@JsonAlias({ "calibrate", "calibration" })
	CALIBRATION,
	@JsonAlias({ "optimize", "optimization" })
	OPTIMIZATION,
	@JsonAlias({ "validation" })
	VALIDATION
}
