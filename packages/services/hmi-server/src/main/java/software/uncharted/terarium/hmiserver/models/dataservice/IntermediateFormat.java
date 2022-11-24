package software.uncharted.terarium.hmiserver.models.dataservice;

public enum IntermediateFormat {
	BILAYER("bilayer"),
	GROMET("gromet"),
	OTHER("other"),
	SBML("sbml");

	public final String type;

	IntermediateFormat(final String type) {
		this.type = type;
	}
}
