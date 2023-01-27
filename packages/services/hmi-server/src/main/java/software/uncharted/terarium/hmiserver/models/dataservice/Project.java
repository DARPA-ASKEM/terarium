package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.documentserver.models.xdd.Document;
import software.uncharted.terarium.hmiserver.models.dataservice.Publication;
import software.uncharted.terarium.hmiserver.models.dataservice.ResourceType;
import software.uncharted.terarium.documentserver.models.xdd.RelatedDocument;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.time.*;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbDateFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
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

	private List<ResourceType> assets;

	private String username;

	@JsonbProperty("relatedArticles")
	@Setter private List<Document> relatedDocuments;

	@Override
    public String toString(){
		return "Project: { id: " + this.projectID +
				" name: " + this.name + 
				" description: " + this.description +
				" Assets: " + this.assets +
				" Related Documents: " + this.relatedDocuments +
				" }";
	}

	

}
