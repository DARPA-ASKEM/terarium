package software.uncharted.terarium.hmiserver.models.task;

import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
public enum TaskStatus {
	CANCELLED,
	COMPLETE,
	ERROR,
	QUEUED,
	RUNNING
}
