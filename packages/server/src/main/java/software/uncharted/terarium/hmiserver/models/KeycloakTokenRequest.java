package software.uncharted.terarium.hmiserver.models;

import feign.form.FormProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
public class KeycloakTokenRequest {

	@Getter
	@FormProperty("grant_type")
	private String grantType = "password";

	@Accessors(chain = true)
	@FormProperty("client_id")
	private String clientId;

	@Accessors(chain = true)
	private String username;

	@Accessors(chain = true)
	private String password;
}
