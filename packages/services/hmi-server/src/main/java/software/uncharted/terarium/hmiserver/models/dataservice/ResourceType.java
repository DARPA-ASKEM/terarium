package software.uncharted.terarium.hmiserver.models.dataservice;

public enum ResourceType {
	DATASETS("datasets"),
	EXTRACTIONS("extractions"),
	INTERMEDIATES("intermediates"),
	MODELS("models"),
	PLANS("plans"),
	PUBLICATIONS("publications"),
	SIMULATION_RUNS("simulation_runs");

	public final String type;

	ResourceType(final String type) {
		this.type = type;
	}
}
