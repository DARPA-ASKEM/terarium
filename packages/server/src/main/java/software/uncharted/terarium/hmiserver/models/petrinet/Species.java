package software.uncharted.terarium.hmiserver.models.petrinet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = SpeciesJsonDeserializer.class)
@Accessors(chain = true)
public class Species implements Serializable {

	private String sname;

	@JsonAlias("mira_ids")
	private List<Ontology> miraIds;

	@JsonAlias("mira_context")
	private List<Ontology> miraContext;
}
