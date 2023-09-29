package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import javax.inject.Inject;

public class RebacGroup extends RebacObject {
	private ReBACService reBACService;

	public RebacGroup(String id, ReBACService reBACService) {
		super(id);
		this.reBACService = reBACService;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.USER, getId());
	}

	public boolean hasMembership(RebacObject rebacObject) throws Exception {
		return reBACService.hasMembership(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public boolean canAdministrate(RebacObject rebacObject) throws Exception {
		return reBACService.canAdministrate(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public void createCreatorRelationship(RebacObject rebacObject) throws Exception {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.CREATOR);
	}

	public void setPermissionRelationships(RebacObject who, String relationship) throws Exception {
		Schema.Relationship relationshipEnum = Schema.Relationship.valueOf(relationship.toUpperCase());
		reBACService.createRelationship(who.getSchemaObject(), getSchemaObject(), relationshipEnum);
	}

	public void removePermissionRelationships(RebacObject who, String relationship) throws Exception, RelationshipAlreadyExistsException {
		Schema.Relationship relationshipEnum = Schema.Relationship.valueOf(relationship.toUpperCase());
		reBACService.removeRelationship(who.getSchemaObject(), getSchemaObject(), relationshipEnum);
	}
}
