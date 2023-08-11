package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Setter;

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

	private String username;

	@JsonAlias("relatedArticles")
	@JsonProperty("relatedDocuments")
	@Setter
	@TSOptional
	private List<Document> relatedDocuments;

	@Override
	public String toString() {
		return "Project: { id: " + this.projectID +
			" name: " + this.name +
			" description: " + this.description +
			" Assets: " + this.assets +
			" Related Documents: " + this.relatedDocuments +
			" }";
	}
}
