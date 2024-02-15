package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@RequiredArgsConstructor
@TSModel
public enum AssetType {
	DATASET("dataset"),
	MODEL_CONFIGURATION("model-configuration"),
	MODEL("model"),
	PUBLICATION("publication"),
	SIMULATION("simulation"),
	WORKFLOW("workflow"),
	ARTIFACT("artifact"),
	CODE("code"),
	DOCUMENT("document");

	@JsonValue
	private final String value;
}
