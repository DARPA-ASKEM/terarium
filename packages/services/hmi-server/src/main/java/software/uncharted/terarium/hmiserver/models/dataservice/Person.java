package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Person implements Serializable {

	private String id;

	private String name;

	private String email;

	@JsonAlias("org")
	private String organization;

	private String website;

	@JsonAlias("is_registered")
	private Boolean isRegistered;
}
