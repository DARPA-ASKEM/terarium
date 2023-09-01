package software.uncharted.terarium.hmiserver.model;

import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
public enum EvaluationScenarioStatus {
	STARTED,
	PAUSED,
	RESUMED,
	STOPPED,
}
