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

	public static final String ID = "id:ID";
	public static final String NAME = "name:string";
	public static final String DESCRIPTION = "description:string";
	public static final String EMBEDDINGS = "description:dense_vector";

	@JsonAlias(ID)
	private String curie;

	@JsonAlias(NAME)
	private String name;

	@JsonAlias(DESCRIPTION)
	private String description;
}
