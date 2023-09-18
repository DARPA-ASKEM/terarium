package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import javax.inject.Inject;

public class RebacUser extends RebacObject {
	private ReBACService reBACService;
	private String id;

	public RebacUser(String id, ReBACService reBACService) {
		this.id = id;
		this.reBACService = reBACService;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.USER, id);
	}

	public PermissionGroup addGroup(String name) throws Exception {
		PermissionGroup group = reBACService.addGroup(name);
		reBACService.createRelationship(getSchemaObject(), new SchemaObject(Schema.Type.GROUP, group.getId()), Schema.Relationship.CREATOR);
		return group;
	}
}
