package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum TaggableType {
	DATASETS("datasets", AssetType.DATASET),
	FEATURES("features", null),
	INTERMEDIATES("intermediates", null),
	MODEL_PARAMETERS("model_parameters", null),
	MODELS("models", AssetType.MODEL),
	PROJECTS("projects", null),
	QUALIFIERS("qualifiers", null),
	SIMULATION_PARAMETERS("simulation_parameters", null),
	SIMULATION_PLANS("simulation_plans", null),
	SIMULATION_RUNS("simulation_runs", null);

	public final String type;
	public final AssetType assetType;

	/**
	 * Returns the enum for a given string representation of a TaggableType
	 *
	 * @param type the string representation of a TaggableType
	 * @return a TaggableType from the type string
	 * @throws IllegalArgumentException if the TaggableType is not found
	 */
	public static TaggableType findByType(final String type) {
		return Arrays.stream(values())
			.filter(value -> type.equalsIgnoreCase(value.type))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No TaggableType with type: " + type));
	}

	TaggableType(final String type, final AssetType assetType) {
		this.type = type;
		this.assetType = assetType;
	}

	public static TaggableType getTaggableTypeFromAssetType(final AssetType assetType) {
		for (final TaggableType type : TaggableType.values()) {
			if (type.assetType != null && type.assetType.equals(assetType)) {
				return type;
			}
		}
		return null;
	}
}
