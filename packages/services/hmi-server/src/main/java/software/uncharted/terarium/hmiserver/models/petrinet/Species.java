package software.uncharted.terarium.hmiserver.models.petrinet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;
import java.util.List;

@Data
@TSModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = SpeciesJsonDeserializer.class)
@Accessors(chain = true)
public class Species implements Serializable {

	private String sname;

	@TSOptional
	@JsonAlias("mira_ids")
	private List<Ontology> miraIds;

	@TSOptional
	@JsonAlias("mira_context")
	private List<Ontology> miraContext;
}
