package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;


@Data
@Accessors(chain = true)
public class Paper {
	private String id;

	@JsonProperty("file_directory")
	private String fileDirectory;

	private String doi;
}

