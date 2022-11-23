package software.uncharted.terarium.hmiserver.models.dataservice;

public enum IntermediateSource {
	MREPRESENTATIONA("mrepresentationa"),
	SKEMA("skema");

	public final String type;

	IntermediateSource(final String type) {
		this.type = type;
	}
}
