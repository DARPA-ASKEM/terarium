package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.hmiserver.models.documentservice.Document;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Setter;

@Data
@Accessors(chain = true)
public class Project implements Serializable {

	@JsonbProperty("id")
	private String projectID;

	private String name;

	private String description;

	private LocalDateTime timestamp;

	private Boolean active;

	private Concept concept;

	private List<String> assets;

	private String username;

	@JsonAlias("relatedDocuments")
	@JsonbProperty("relatedArticles")
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
