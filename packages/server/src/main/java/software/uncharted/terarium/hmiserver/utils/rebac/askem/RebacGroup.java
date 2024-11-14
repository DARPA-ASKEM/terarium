package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

public class RebacGroup extends RebacObject {

	private ReBACService reBACService;

	public RebacGroup(String id, ReBACService reBACService) {
		super(id);
		this.reBACService = reBACService;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.GROUP, getId());
	}

	public boolean hasMembership(RebacGroup rebacGroup) throws Exception {
		return reBACService.isMemberOf(getSchemaObject(), rebacGroup.getSchemaObject());
	}

	public boolean canAdministrate(RebacObject rebacObject) throws Exception {
		return reBACService.can(getSchemaObject(), Schema.Permission.ADMINISTRATE, rebacObject.getSchemaObject());
	}

	public void createReaderRelationship(RebacObject rebacObject) throws Exception, RelationshipAlreadyExistsException {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.READER);
	}

	public void createWriterRelationship(RebacObject rebacObject) throws Exception, RelationshipAlreadyExistsException {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.WRITER);
	}

	public void createCreatorRelationship(RebacObject rebacObject) throws Exception, RelationshipAlreadyExistsException {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.CREATOR);
	}

	public void createAdminRelationship(RebacObject rebacObject) throws Exception, RelationshipAlreadyExistsException {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.ADMIN);
	}

	/** Remove all relationships between this group and the given object. */
	public void removeAllRelationships(final RebacObject rebacObject) throws Exception {
		final Schema.Relationship[] relationships = {
			Schema.Relationship.READER,
			Schema.Relationship.WRITER,
			Schema.Relationship.CREATOR,
			Schema.Relationship.ADMIN
		};
		for (Schema.Relationship relationship : relationships) {
			reBACService.removeRelationship(getSchemaObject(), rebacObject.getSchemaObject(), relationship);
		}
	}

	/**
	 * Remove all relationships between this group and the given object.
	 * Except the provided relationship, if it doesn't exist, it will be added
	 */
	public void removeAllRelationsExceptOne(
		final RebacObject rebacObject,
		final Schema.Relationship relationshipToIgnore
	) throws Exception {
		final Schema.Relationship[] relationships = {
			Schema.Relationship.READER,
			Schema.Relationship.WRITER,
			Schema.Relationship.CREATOR,
			Schema.Relationship.ADMIN
		};
		for (Schema.Relationship relationship : relationships) {
			if (relationship == relationshipToIgnore) {
				try {
					reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), relationship);
				} catch (RelationshipAlreadyExistsException ignore) {}
			} else {
				reBACService.removeRelationship(getSchemaObject(), rebacObject.getSchemaObject(), relationship);
			}
		}
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
}
