package software.uncharted.terarium.hmiserver.models.petrinet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = SpeciesJsonDeserializer.class)
public class Species implements Serializable {

	@JsonAlias("sname")
	private String name;

	@JsonAlias("mira_ids")
	private List<Ontology> miraIds;

	@JsonAlias("mira_context")
	private List<Ontology> miraContext;
}
