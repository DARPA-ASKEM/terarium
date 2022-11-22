package software.uncharted.terarium.hmiserver.models.dataservice;

public enum OntologicalField {
	OBJECT("obj"),
	UNIT("unit");

	public final String type;

	OntologicalField(final String type) {
		this.type = type;
	}
}
