package software.uncharted.terarium.hmiserver;

import com.fasterxml.jackson.annotation.JsonAlias;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
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
