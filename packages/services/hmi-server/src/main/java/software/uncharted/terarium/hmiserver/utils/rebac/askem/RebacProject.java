package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.AskemDatumType;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

public class RebacProject extends RebacObject {
	private final AskemDatumType datumType = AskemDatumType.PROJECT;
	private String id;

	public RebacProject(String id) {
		this.id = id;
	}

	public SchemaObject getSchemaObject() {
		return new SchemaObject(Schema.Type.DATUM, datumType + id);
	}
}
