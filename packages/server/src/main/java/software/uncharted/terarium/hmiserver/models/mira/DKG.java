package software.uncharted.terarium.hmiserver.models.mira;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@TSModel
public class DKG {

	@JsonAlias("id")
	private String curie;

	private String name;
	private String description;
	private String link;
}
