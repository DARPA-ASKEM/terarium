package software.uncharted.terarium.hmiserver.utils.rebac;

public class SchemaObject {

	Schema.Type type;
	String id;

	public SchemaObject(Schema.Type type, String id) {
		this.type = type;
		this.id = id;
	}
}
