package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.AskemDatumType;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
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
		return reBACService.getPermissions(getSchemaObject());
	}
}
