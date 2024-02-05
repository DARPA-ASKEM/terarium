package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum SimulationType {
	@JsonAlias("ensemble-simulate")
	ENSEMBLE,
	@JsonAlias({"simulate", "simulation"})
	SIMULATION,
	@JsonAlias("calibrate")
	CALIBRATION
}
