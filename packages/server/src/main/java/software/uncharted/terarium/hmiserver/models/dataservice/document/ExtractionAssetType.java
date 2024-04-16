package software.uncharted.terarium.hmiserver.models.dataservice.document;

public enum ExtractionAssetType {
	FIGURE("figure"),
	TABLE("table"),
	EQUATION("equation");

	public final String type;

	ExtractionAssetType(final String type) {
		this.type = type.toLowerCase();
	}

	public String toString() {
		return type.toLowerCase();
	}

	public String toStringPlural() {
		return toString() + "s";
	}

	public static ExtractionAssetType fromString(final String type) {
		for (final ExtractionAssetType assetType : ExtractionAssetType.values()) {
			if (assetType.toString().equalsIgnoreCase(type)) {
				return assetType;
			}
		}
		throw new IllegalArgumentException("No constant with type " + type + " found");
	}
}
