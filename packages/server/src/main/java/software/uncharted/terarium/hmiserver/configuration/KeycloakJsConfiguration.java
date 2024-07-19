package software.uncharted.terarium.hmiserver.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Accessors(chain = true)
@Data
public class KeycloakJsConfiguration implements Serializable {

	@JsonProperty("auth-server-url")
	private String url;

	private String realm;

	@JsonProperty("resource")
	private String clientId;

	private String responseMode = "query";
	/** If true, signing out in one window signs out other windows */
	private Boolean checkLoginIFrame = true;
}
