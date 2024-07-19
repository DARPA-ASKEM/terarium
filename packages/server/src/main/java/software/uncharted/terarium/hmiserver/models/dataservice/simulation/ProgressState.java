package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum ProgressState {
	@JsonAlias("cancelled")
	CANCELLED,
	@JsonAlias("complete")
	COMPLETE,
	@JsonAlias("error")
	ERROR,
	@JsonAlias("failed")
	FAILED,
	@JsonAlias("queued")
	QUEUED,
	@JsonAlias("retrieving")
	RETRIEVING,
	@JsonAlias("running")
	RUNNING,
	@JsonAlias("cancelling")
	CANCELLING
}
