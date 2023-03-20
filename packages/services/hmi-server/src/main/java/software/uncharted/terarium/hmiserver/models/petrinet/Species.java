package software.uncharted.terarium.hmiserver.models.petrinet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import software.uncharted.terarium.hmiserver.models.Ontology;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = SpeciesJsonDeserializer.class)
public class Species implements Serializable {

	/*
		{
			"sname": "S",
			"mira_ids": "[('identity', 'ido:0000514'), ('identity', 'ido:0000511')]",
			"mira_context": [('identity', 'ido:0000514')]",
		}
	 */

	@JsonAlias("sname")
	private String name;

	@JsonAlias("mira_ids")
	private List<Ontology> miraIds;

	@JsonAlias("mira_context")
	private List<Ontology> miraContext;
}
