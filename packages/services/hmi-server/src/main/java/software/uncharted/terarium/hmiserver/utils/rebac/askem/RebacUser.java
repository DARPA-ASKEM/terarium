package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import javax.inject.Inject;

public class RebacUser {
	private ReBACService reBACService;
	private String id;

	public RebacUser(String id, ReBACService reBACService) {
		this.id = id;
		this.reBACService = reBACService;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.USER, id);
	}

	public boolean canRead(RebacProject rebacProject) throws Exception {
		return reBACService.canRead(getSchemaObject(), rebacProject.getSchemaObject());
	}

	public boolean canWrite(RebacProject rebacProject) throws Exception {
		return reBACService.canWrite(getSchemaObject(), rebacProject.getSchemaObject());
	}

	public boolean canAdministrate(RebacProject rebacProject) throws Exception {
		return reBACService.canAdministrate(getSchemaObject(), rebacProject.getSchemaObject());
	}

	public void createCreatorRelationship(RebacProject rebacProject) throws Exception {
		reBACService.createRelationship(getSchemaObject(), rebacProject.getSchemaObject(), Schema.Relationship.CREATOR);
	}
}
