package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@RequiredArgsConstructor
@TSModel
public enum AssetType {
	WORKFLOW("workflow"),
	MODEL("model"),
	DATASET("dataset"),
	SIMULATION("simulation"),
	DOCUMENT("document"),
	CODE("code"),
	MODEL_CONFIGURATION("model-configuration"),
	ARTIFACT("artifact"),
	PUBLICATION("publication"),
	NOTEBOOK_SESSION("notebook-session"),
	;

	@JsonValue
	private final String value;
}
