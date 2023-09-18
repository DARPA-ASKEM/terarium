package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import javax.inject.Inject;

public class RebacGroup extends RebacObject {
	@Inject
	ReBACService reBACService;

	private String id;

	public RebacGroup(String id) {
		this.id = id;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.USER, id);
	}
}
