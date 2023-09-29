package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.AskemDatumType;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import javax.inject.Inject;
import java.util.List;

public class RebacProject extends RebacObject {
	@Inject
	ReBACService reBACService;

	private final AskemDatumType datumType = AskemDatumType.PROJECT;

	public RebacProject(String id, ReBACService reBACService) {
		super(id);
		this.reBACService = reBACService;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.DATUM, datumType + getId());
	}

	public List<RebacPermissionRelationship> getPermissionRelationships() throws Exception {
		return reBACService.getRelationships(getSchemaObject());
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
