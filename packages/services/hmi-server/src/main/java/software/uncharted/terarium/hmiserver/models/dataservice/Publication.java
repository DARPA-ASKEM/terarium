package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Publication implements Serializable {

	private Long id;

	@JsonAlias("xdd_uri")
	private String xddUri;

	@JsonAlias("title")
	private String title;

}
