package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

@Slf4j
public class RebacUser extends RebacObject {

	private final ReBACService reBACService;

	private final boolean serviceUser;
	private final boolean adminServiceUser;

	public RebacUser(final String id, final ReBACService reBACService) {
		super(id);
		this.reBACService = reBACService;

		serviceUser = ReBACService.isServiceUser(id);
		adminServiceUser = ReBACService.isAdminServiceUser(id);
	}

	@Override
	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.USER, getId());
	}

	public boolean isAdmin() {
		try {
			boolean permission = reBACService.isMemberOf(
				getSchemaObject(),
				new RebacGroup(ReBACService.ASKEM_ADMIN_GROUP_ID, reBACService).getSchemaObject()
			);

			return (adminServiceUser || permission);
		} catch (final Exception e) {
			log.error("Error checking if user is admin", e);
			return false;
		}
	}

	public boolean can(final RebacObject rebacObject, final Schema.Permission permission) throws Exception {
		if (serviceUser || isAdmin()) return true;
		if (rebacObject.getId().isEmpty()) return false;
		return reBACService.can(getSchemaObject(), permission, rebacObject.getSchemaObject());
	}

	public boolean isMemberOf(final RebacGroup rebacGroup) throws Exception {
		if (isAdmin()) {
			return true;
		}
		return reBACService.isMemberOf(getSchemaObject(), rebacGroup.getSchemaObject());
	}

	public void createCreatorRelationship(final RebacObject rebacObject)
		throws Exception, RelationshipAlreadyExistsException {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.CREATOR);
	}

	public PermissionGroup createGroup(final String name) throws Exception, RelationshipAlreadyExistsException {
		final PermissionGroup group = reBACService.createGroup(name);
		reBACService.createRelationship(
			getSchemaObject(),
			new SchemaObject(Schema.Type.GROUP, group.getId()),
			Schema.Relationship.CREATOR
		);
		return group;
	}

	public String getPermissionFor(final RebacProject rebacProject) throws Exception {
		if (reBACService.isCreator(getSchemaObject(), rebacProject.getSchemaObject())) {
			return Schema.Relationship.CREATOR.toString();
		} else if (can(rebacProject, Schema.Permission.WRITE)) {
			return Schema.Relationship.WRITER.toString();
		} else if (can(rebacProject, Schema.Permission.READ)) {
			return Schema.Relationship.READER.toString();
		} else if (isAdmin() || can(rebacProject, Schema.Permission.ADMINISTRATE)) {
			return Schema.Relationship.ADMIN.toString();
		}
		return "none";
	}

	public List<UUID> lookupProjects() throws Exception {
		return reBACService.lookupResources(getSchemaObject(), Schema.Permission.READ, Schema.Type.PROJECT);
	}
}
