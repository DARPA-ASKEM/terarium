package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.Arrays;


/**
 * This will be removed in favour of ResourceType
 */
@Deprecated
@TSModel
public enum AssetType {
	datasets("datasets"),
	model_configurations("model_configurations"),
	models("models"),
	publications("publications"),
	simulations("simulations"),
	workflows("workflows"),
	artifacts("artifacts"),
	code("code"),
	documents("documents");


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
