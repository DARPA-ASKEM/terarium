package software.uncharted.terarium.hmiserver.models.dataservice.document;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.UserId;
import software.uncharted.terarium.hmiserver.models.data.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

@Data
@TSModel
@Accessors(chain = true)
public class DocumentAsset {

	@TSOptional
	private UUID id;

	@TSOptional
	private String name;

	@TSOptional
	private String description;

	@TSOptional
	private String timestamp;

	@TSOptional
	private UserId userId;

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

}
