package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;

public abstract class RebacObject {
    private String id;

    public RebacObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract SchemaObject getSchemaObject();
}
