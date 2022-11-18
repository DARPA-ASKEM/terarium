package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Person implements Serializable {

	private String id;

	private String name;

	private String email;

	@JsonbProperty("org")
	private String organization;

	private String website;

	@JsonbProperty("is_registered")
	private Boolean isRegistered;
}
