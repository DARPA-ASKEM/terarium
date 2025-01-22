package software.uncharted.terarium.hmiserver.models.mira;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@TSModel
@AllArgsConstructor
@RequiredArgsConstructor
public class DKG {

	public static final String EMBEDDINGS = "description:dense_vector";
	public static final String GEONAMES = "geonames";

	public DKG(String curie) {
		this.curie = curie;
	}

	@JsonAlias(Grounding.ID)
	private String curie;

	@JsonAlias(Grounding.NAME)
	private String name;

	@JsonAlias(Grounding.DESCRIPTION)
	@JsonSetter(nulls = Nulls.AS_EMPTY)
	private String description = "";
}
