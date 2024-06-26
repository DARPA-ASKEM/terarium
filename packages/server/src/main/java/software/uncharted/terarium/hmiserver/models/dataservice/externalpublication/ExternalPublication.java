package software.uncharted.terarium.hmiserver.models.dataservice.externalpublication;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class ExternalPublication extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -1717000256225044631L;

	@JsonProperty("xdd_uri")
	private String xddUri;

	@Schema(defaultValue = "Article title")
	private String title;
}
