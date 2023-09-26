package software.uncharted.terarium.hmiserver.models;

import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
public enum EvaluationScenarioStatus {
	STARTED,
	PAUSED,
	RESUMED,
	STOPPED,
}
