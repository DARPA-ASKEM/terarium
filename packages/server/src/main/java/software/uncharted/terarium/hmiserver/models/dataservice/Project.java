package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@TSModel
public class Project implements Serializable {

	@JsonProperty("id")
	@TSOptional
	private String projectID;

	private String name;

	@TSOptional
	private String description;

	@TSOptional
	private LocalDateTime timestamp;

	private Boolean active;

	@TSOptional
	private Concept concept;

	@TSOptional
	private Assets assets;

	@TSOptional
	// Metadata that can be useful for the UI
	private Map<String, String> metadata;

	private String username;

	@JsonAlias("relatedArticles")
	@JsonProperty("relatedDocuments")
	@Setter
	@TSOptional
	private List<Document> relatedDocuments;

	@TSOptional
	private Boolean publicProject;

	@TSOptional
	private String userPermission;
}
