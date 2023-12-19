package software.uncharted.terarium.hmiserver.models.dataservice;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.OntologyConcept;

/**
 * Represents a generic artifact that can be stored in the data service. For
 * example,
 * this could be a text file, a code file, a zip file, or anything else. It
 * should not
 * be used for a dataset or a model, which have their own classes.
 */
@Data
@Accessors(chain = true)
@TSModel
public class Artifact {

	/* The id of the artifact. */
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp createdOn;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp deletedOn;

	/* UserId of who created this asset */
	private String userId;

	/* The name of the artifact. */
	private String name;

	/* A description of the artifact. */
	@TSOptional
	private String description;

	/* The name of the file(s) in this artifact */
	@JsonAlias("file_names")
	private List<String> fileNames;

	/* metadata for these files */
	@TSOptional
	private JsonNode metadata;

	/* concepts associated with these files */
	@TSOptional
	private List<OntologyConcept> concepts;

}
