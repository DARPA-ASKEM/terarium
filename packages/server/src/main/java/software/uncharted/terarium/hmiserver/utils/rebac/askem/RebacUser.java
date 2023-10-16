package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

public class RebacUser extends RebacObject {
	private ReBACService reBACService;

	public RebacUser(String id, ReBACService reBACService) {
		super(id);
		this.reBACService = reBACService;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.USER, getId());
	}

	public boolean canRead(RebacObject rebacObject) throws Exception {
		return reBACService.canRead(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public boolean canWrite(RebacObject rebacObject) throws Exception {
		return reBACService.canWrite(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public boolean canAdministrate(RebacObject rebacObject) throws Exception {
		return reBACService.canAdministrate(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public void createCreatorRelationship(RebacObject rebacObject) throws Exception, RelationshipAlreadyExistsException {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.CREATOR);
	}

	public PermissionGroup createGroup(String name) throws Exception, RelationshipAlreadyExistsException {
		PermissionGroup group = reBACService.createGroup(name);
		reBACService.createRelationship(getSchemaObject(), new SchemaObject(Schema.Type.GROUP, group.getId()), Schema.Relationship.CREATOR);
		return group;
	}

	public String getPermissionFor(RebacObject rebacObject) throws Exception {
		if (reBACService.isCreator(getSchemaObject(), rebacObject.getSchemaObject())) {
			return Schema.Relationship.CREATOR.toString();
		} else if (canAdministrate(rebacObject)) {
			return Schema.Relationship.ADMIN.toString();
		} else if (canWrite(rebacObject)) {
			return Schema.Relationship.WRITER.toString();
		} else if (canRead(rebacObject)) {
			return Schema.Relationship.READER.toString();
		}
		return "none";
	}
}
