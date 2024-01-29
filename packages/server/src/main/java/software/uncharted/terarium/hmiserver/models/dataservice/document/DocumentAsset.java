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

import java.util.HashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
public class DocumentAsset extends TerariumAsset {


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

}
