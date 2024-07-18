package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import java.util.List;
import java.util.UUID;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

public class RebacProject extends RebacObject {

	ReBACService reBACService;

	public RebacProject(UUID id, ReBACService reBACService) {
		super(id);
		this.reBACService = reBACService;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.PROJECT, getId());
	}

	public List<RebacPermissionRelationship> getPermissionRelationships() throws Exception {
		return reBACService.getRelationships(getSchemaObject());
	}

	public void setPermissionRelationships(RebacObject who, String relationship)
		throws Exception, RelationshipAlreadyExistsException {
		Schema.Relationship relationshipEnum = Schema.Relationship.valueOf(relationship.toUpperCase());
		reBACService.createRelationship(who.getSchemaObject(), getSchemaObject(), relationshipEnum);
	}

	public void removePermissionRelationships(RebacObject who, String relationship)
		throws Exception, RelationshipAlreadyExistsException {
		Schema.Relationship relationshipEnum = Schema.Relationship.valueOf(relationship.toUpperCase());
		reBACService.removeRelationship(who.getSchemaObject(), getSchemaObject(), relationshipEnum);
	}

	public boolean isPublic() throws Exception {
		List<RebacPermissionRelationship> relationships = reBACService.getRelationships(getSchemaObject());
		for (RebacPermissionRelationship relationship : relationships) {
			if (
				relationship.getSubjectType().equals(Schema.Type.GROUP) &&
				relationship.getSubjectId().equals(ReBACService.PUBLIC_GROUP_ID)
			) {
				if (
					relationship.getRelationship().equals(Schema.Relationship.READER) ||
					relationship.getRelationship().equals(Schema.Relationship.WRITER)
				) {
					return true;
				}
			}
		}
		return false;
	}
}
