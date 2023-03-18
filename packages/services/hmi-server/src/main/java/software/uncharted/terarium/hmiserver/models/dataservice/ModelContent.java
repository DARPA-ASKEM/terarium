package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class ModelContent implements Serializable {
	private Map<String, String>[] S;

	private Map<String, Optional<String>>[] T;

	private Map<String, Number>[] I;

	private Map<String, Number>[] O;

	/**
	 * Resolve species curies
	 * @param species A PetriNet Species
	 * @return a species as String, with resolved curies
	 *
	 * Example:
	 * input - Array of string tuple
	 * 		[
	 * 			('disease_severity', 'ncit:C25269'),
	 * 			('hospitalization_status', 'ncit:C25179')
	 * 		]
	 * output - Array of objects
	 * 		[
	 * 			{
	 * 				name: 'disease_severity',
	 * 				curie: 'ncit:C25269',
	 * 				title: 'Symptomatic',
	 * 				description: 'Exhibiting the symptoms of a particular disease.',
	 * 				url: 'https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=C25269'
	 * 			},
	 * 			{
	 * 				name: 'hospitalization_status',
	 * 				curie: 'ncit:C25179',
	 * 				title: 'Hospitalization',
	 * 				description: 'The condition of being treated as a patient in a hospital.',
	 * 				url: 'https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=C25179'
	 * 			}
	 * 		]
	 */
	public String curieResolver(final String species) {
		// TODO - how to parse a string to Java Object
		return species;
	}
}
