package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.Ontology;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {

	@JsonAlias("S")
	private List<Map<String, String>> species;

	@JsonAlias("T")
	private List<Map<String, Optional<String>>> transitions;

	@JsonAlias("I")
	private List<Map<String, Number>> input;

	@JsonAlias("O")
	private List<Map<String, Number>> output;

	/**
	 * Resolve species curies
	 * @return a species as String, with resolved curies
	 *
	 * Example:
	 * input - Array of string tuple
	 * 		[
	 *             {
	 *                 "sname": "S",
	 *                 "mira_ids": "[('identity', 'ido:0000514')]",
	 *                 "mira_context": "[
	 * 														('disease_severity', 'ncit:C25269'),
	 * 														('hospitalization_status', 'ncit:C25179')
	 * 									]
	 * 							}
	 * 		]
	 * output - Array of objects
	 * 		ontology
	 * 		context: [
	 * 			{
	 * 				name: 'disease_severity',
	 * 				ontology: {
	 * 					curie: 'ncit:C25269',
	 * 					title: 'Symptomatic',
	 * 					description: 'Exhibiting the symptoms of a particular disease.',
	 * 					url: 'https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=C25269'
	 * 				}
	 * 			},
	 * 			{
	 * 				name: 'hospitalization_status',
	 * 				ontology: {
	 * 					curie: 'ncit:C25179',
	 * 					title: 'Hospitalization',
	 * 					description: 'The condition of being treated as a patient in a hospital.',
	 * 					url: 'https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=C25179'
	 * 				}
	 * 			}
	 * 		]
	 */
	public void curieResolver() {
		ObjectMapper ontologiesMapper = new ObjectMapper();
		BiFunction resolver = (key, value) -> {
			try {
				return ontologiesMapper.readValue((JsonParser) value, Ontology.class);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};

		List<Ontology> curies = new ArrayList<>();
		for (Map<String, String> specie : species) {
			// for now only check for the keys 'mira_ids' and 'mira_context'
			specie.computeIfPresent("mira_ids", resolver);
			specie.computeIfPresent("mira_context", resolver);
		}
	}
}
