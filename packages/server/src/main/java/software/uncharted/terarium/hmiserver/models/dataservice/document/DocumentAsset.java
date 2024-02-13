package software.uncharted.terarium.hmiserver.models.dataservice.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
public class DocumentAsset extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -8425680186002783351L;

	@TSOptional
	private String name;

	@TSOptional
	private String description;

	@TSOptional
	private String userId;

	@TSOptional
	@JsonAlias("file_names")
	private List<String> fileNames;

	@TSOptional
	@JsonAlias("document_url")
	private String documentUrl;

	@TSOptional
	private HashMap<String, Object> metadata;

	@TSOptional
	private String source;

	@TSOptional
	private String text;

	@TSOptional
	private Grounding grounding;

	@TSOptional
	private List<OntologyConcept> concepts;

	@TSOptional
	private List<DocumentExtraction> assets;

	/**
	 * Get the DOI of a document
	 *
	 * @param doc
	 * @return the DOI of the document, or an empty string if no DOI is found
	 */
	public static String getDocumentDoi(final Document doc) {
		String docIdentifier = "";
		if (doc != null && doc.getIdentifier() != null && !doc.getIdentifier().isEmpty()) {
			for (final Map<String, String> identifier : doc.getIdentifier()) {
				if (identifier.get("type").equals("doi")) {
					docIdentifier = identifier.get("id");
					break;
				}
			}
		}
		return docIdentifier;
	}
}
