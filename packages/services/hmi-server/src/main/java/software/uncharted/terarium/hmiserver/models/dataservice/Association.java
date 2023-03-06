package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;


import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Association implements Serializable {

	private String id;

	@JsonAlias("person_id")
	private String personId;

	@JsonAlias("resource_id")
	private String resourceId;

	@JsonAlias("resource_type")
	private ResourceType.Type resourceType;

	private Role role;
}
