package software.uncharted.terarium.hmiserver.utils.rebac;

import java.util.Objects;

public class SchemaObject {

	Schema.Type type;
	String id;

	public SchemaObject(Schema.Type type, String id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SchemaObject)) {
			return false;
		}
		SchemaObject other = (SchemaObject) o;
		return id.equals(other.id) && type == other.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, id);
	}
}
