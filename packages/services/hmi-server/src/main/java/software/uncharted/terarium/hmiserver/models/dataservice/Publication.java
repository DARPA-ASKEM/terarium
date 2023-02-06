package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Publication extends ResourceType implements Serializable {


	@JsonbProperty("xdd_uri")
	private String xddUri;

	@JsonbProperty("title")
	private String title;

}
