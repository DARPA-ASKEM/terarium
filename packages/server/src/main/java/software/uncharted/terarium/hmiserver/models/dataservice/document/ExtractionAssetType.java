package software.uncharted.terarium.hmiserver.models.dataservice.document;

public enum ExtractionAssetType {
	FIGURE("figure"),
	TABLE("table"),
	EQUATION("equation");

	public final String type;

	ExtractionAssetType(final String type) {
		this.type = type.toLowerCase();
	}

	public static ExtractionAssetType fromString(String type) {
		for (ExtractionAssetType assetType : ExtractionAssetType.values()) {
			if (assetType.toString().equalsIgnoreCase(type)) {
				return assetType;
			}
		}
		throw new IllegalArgumentException("No constant with type " + type + " found");
	}
}
