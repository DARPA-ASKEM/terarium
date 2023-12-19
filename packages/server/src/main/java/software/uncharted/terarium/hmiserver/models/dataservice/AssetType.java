package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.Arrays;

@TSModel
public enum AssetType {
	DATASET("dataset"),
	MODEL_CONFIGURATION("model_configuration"),
	MODEL("model"),
	PUBLICATION("publication"),
	SIMULATION("simulation"),
	WORKFLOW("workflow"),
	ARTIFACT("artifact"),
	CODE("code"),
	DOCUMENT("document");


	public final String type;


	AssetType(final String type) {
		this.type = type.toLowerCase();
	}

	@Override
	public String toString() {
		return type;
	}

	@JsonCreator
	public static AssetType fromString(final String type) {
		return Arrays.stream(AssetType.values())
			.filter(t -> t.type.equalsIgnoreCase(type))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown resource type: " + type));
	}
}
