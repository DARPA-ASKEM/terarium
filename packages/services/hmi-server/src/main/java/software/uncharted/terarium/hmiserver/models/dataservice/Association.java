package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Association implements Serializable {

	private String id;

	@JsonbProperty("person_id")
	private String personId;

	@JsonbProperty("resource_id")
	private String resourceId;

	@JsonbProperty("resource_type")
	private ResourceType resourceType;

	private Role role;
}
