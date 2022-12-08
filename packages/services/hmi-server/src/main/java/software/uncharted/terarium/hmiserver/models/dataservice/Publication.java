package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Publication implements Serializable {

	private String id;

	@JsonbProperty("xdd_uri")
	private String xddUri;

	@JsonbProperty("title")
	private String title;
}
