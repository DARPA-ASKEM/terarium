package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Setter;

@Data
@Accessors(chain = true)
public class Project implements Serializable {

	@JsonProperty("id")
	private String projectID;

	private String name;

	private String description;

	private LocalDateTime timestamp;

	private Boolean active;

	private Concept concept;

	private Map<String, List<String>> assets;

	private String username;

	@JsonAlias("relatedArticles")
	@JsonProperty("relatedDocuments")
	@Setter
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
