package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

import javax.inject.Inject;

public abstract class RebacObject {
	@Inject
	ReBACService reBACService;

	public abstract SchemaObject getSchemaObject();

	public boolean canRead(RebacObject rebacObject) throws Exception {
		return reBACService.canRead(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public boolean canWrite(RebacObject rebacObject) throws Exception {
		return reBACService.canWrite(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public boolean canAdministrate(RebacObject rebacObject) throws Exception {
		return reBACService.canAdministrate(getSchemaObject(), rebacObject.getSchemaObject());
	}

	public void createCreatorRelationship(RebacObject rebacObject) throws Exception {
		reBACService.createRelationship(getSchemaObject(), rebacObject.getSchemaObject(), Schema.Relationship.CREATOR);
	}
}
