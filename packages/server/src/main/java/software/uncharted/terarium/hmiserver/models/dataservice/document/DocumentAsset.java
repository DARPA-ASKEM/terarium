package software.uncharted.terarium.hmiserver.models.dataservice.document;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.data.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

@Data
@TSModel
@Accessors(chain = true)
public class DocumentAsset {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@TSOptional
	private String name;

	@TSOptional
	private String description;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp createdOn;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp deletedOn;

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
