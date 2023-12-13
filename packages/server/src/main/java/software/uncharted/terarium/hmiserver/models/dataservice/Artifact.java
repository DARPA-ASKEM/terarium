package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.UserId;

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
	@TSOptional
	private String id;

	/* Timestamp of creation */
	@TSOptional
	private Object timestamp;

	/* UserId of who created this asset */
	private UserId userId;

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
	private List<Concept> concepts;

}
