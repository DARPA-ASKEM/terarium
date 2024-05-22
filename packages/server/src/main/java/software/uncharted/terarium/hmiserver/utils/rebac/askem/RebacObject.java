package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import java.util.UUID;

public abstract class RebacObject {
	private String id;

	public RebacObject(String id) {
		this.id = id;
	}

	public RebacObject(UUID id) {
		if (id == null) {
			this.id = "";
		} else {
			this.id = id.toString();
		}
	}

	public String getId() {
		return id;
	}

	public abstract SchemaObject getSchemaObject();
}
