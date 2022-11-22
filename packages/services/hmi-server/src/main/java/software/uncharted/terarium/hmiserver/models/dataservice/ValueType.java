package software.uncharted.terarium.hmiserver.models.dataservice;

public enum ValueType {
	BINARY("binary"),
	BOOL("bool"),
	FLOAT("float"),
	INT("int"),
	STR("str");

	public final String type;

	ValueType(final String type) {
		this.type = type;
	}
}
