package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;


@Data
@Accessors(chain = true)
public class Paper {
	private String id;

	@JsonAlias("file_directory")
	private String fileDirectory;

	private String doi;
}

