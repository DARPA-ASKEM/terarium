package software.uncharted.terarium.hmiserver.models.data;


public enum ResourceType {
	ARTIFACT("artifact"),
	CODE("code"),
	DATASET("dataset"),
	DOCUMENT("document"),
	EQUATION("equation"),
	MODEL("model"),
	MODEL_CONFIGURATION("model_configuration"),
	PUBLICATION("publication"),
	SIMULATION("simulation"),
	WORKFLOW("workflow");

	final private String resourceTypeString;

	ResourceType(final String str) {
		this.resourceTypeString = str;
	}

	@Override
	public String toString() {
		return this.resourceTypeString;
	}
}
