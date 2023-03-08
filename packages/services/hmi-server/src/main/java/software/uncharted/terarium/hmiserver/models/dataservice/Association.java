package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;


import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Association implements Serializable {

	private String id;

	@JsonProperty("person_id")
	private String personId;

	@JsonProperty("resource_id")
	private String resourceId;

	@JsonProperty("resource_type")
	private ResourceType.Type resourceType;

	private Role role;
}
