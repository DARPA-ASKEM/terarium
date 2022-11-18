package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class Association {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("person_id")
	public String personId;

	@JsonbProperty("resource_id")
	public String resourceId;

	@JsonbProperty("resource_type")
	public ResourceType resourceType;

	@JsonbProperty("role")
	public Role role;

	public Association(final String personId, final String resourceId, final ResourceType resourceType, final Role role) {
		this.personId = personId;
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.role = role;
	}
}
