package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import javax.inject.Inject;

public class RebacGroup {
	@Inject
	ReBACService reBACService;

	private String id;

	public RebacGroup(String id) {
		this.id = id;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.USER, id);
	}

	public boolean canRead(SchemaObject schemaObject) throws Exception {
		return reBACService.canRead(getSchemaObject(), schemaObject);
	}

	public boolean canWrite(SchemaObject schemaObject) throws Exception {
		return reBACService.canWrite(getSchemaObject(), schemaObject);
	}

	public boolean canAdministrate(SchemaObject schemaObject) throws Exception {
		return reBACService.canAdministrate(getSchemaObject(), schemaObject);
	}

	public void createCreatorRelationship(SchemaObject schemaObject) throws Exception {
		reBACService.createRelationship(getSchemaObject(), schemaObject, Schema.Relationship.CREATOR);
	}
}
