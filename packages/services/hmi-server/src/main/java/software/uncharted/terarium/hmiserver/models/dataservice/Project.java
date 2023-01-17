package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.documentserver.models.xdd.Document;
import software.uncharted.terarium.hmiserver.models.dataservice.Publication;
import software.uncharted.terarium.hmiserver.models.dataservice.ResourceType;

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

	@JsonbProperty("relatedArticles")
	private List<Document> relatedDocuments;

	public Project(String id, String name, String description) {
		this.projectID = id;
		this.name = name;
		this.description = description;
		this.active = true;
		// this.timestamp = Instant.parse(timestamp + "Z");
		
	}

	public Project(){} //Required default constructor for the data annotation
	
	public String getID(){ return this.projectID; }

	public void setRelatedDocuments(List<Document> relatedDocuments){
		this.relatedDocuments = relatedDocuments;
	}

	public void setAssets(List<ResourceType> assets){
		this.assets = assets;
	}

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
