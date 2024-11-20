package software.uncharted.terarium.hmiserver.models;

public enum TerariumAssetEmbeddingType {
	OVERVIEW("overview"),
	NAME("name"),
	DESCRIPTION("description"),
	METADATA("metadata"),
	CARD("card");

	private final String text;

	TerariumAssetEmbeddingType(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
