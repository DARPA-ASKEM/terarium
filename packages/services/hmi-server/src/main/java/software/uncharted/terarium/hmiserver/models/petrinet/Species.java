package software.uncharted.terarium.hmiserver.models.petrinet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = SpeciesJsonDeserializer.class)
public class Species implements Serializable {

	@JsonAlias("sname")
	private String name;

	private int uid;

	@JsonAlias("mira_ids")
	private List<Ontology> miraIds;

	@JsonAlias("mira_context")
	private List<Ontology> miraContext;

	/**
	 * Returns all the curies of all ontologies within the Species.
	 * @return List of curies
	 */
	public List<String> getAllCuries() {
		return Stream.concat(
			miraIds.stream().map(Ontology::getCurie),
			miraContext.stream().map(Ontology::getCurie)
		).toList();
	}
}
