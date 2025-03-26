package software.uncharted.terarium.taskrunner.models.task;

import com.fasterxml.jackson.annotation.JsonAlias;

// TODO delete and reuse software.uncharted.terarium.hmiserver.ProgressState
public enum ProgressState {
	@JsonAlias("CANCELLED")
	CANCELLED,
	@JsonAlias("COMPLETE")
	COMPLETE,
	@JsonAlias("ERROR")
	ERROR,
	@JsonAlias("QUEUED")
	QUEUED,
	@JsonAlias("RUNNING")
	RUNNING
}
