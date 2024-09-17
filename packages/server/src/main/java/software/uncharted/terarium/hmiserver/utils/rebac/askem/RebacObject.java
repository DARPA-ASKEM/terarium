package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import java.util.UUID;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

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
