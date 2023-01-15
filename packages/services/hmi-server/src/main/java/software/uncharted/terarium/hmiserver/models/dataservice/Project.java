package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.documentserver.models.xdd.Document;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.time.*;



@Data
@Accessors(chain = true)
public class Project implements Serializable {

	private String id;

	private String name;

	private String description;

	private Instant timestamp;

	private Boolean active;

	private Concept concept;

	private Map<String, List<Long>> assets;

	private Document relatedDocuments; //TODO: Replace with document-service's document class

	public Project(String id, String name, String description,String timestamp) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.active = true;
		this.timestamp = Instant.parse(timestamp + "Z");
		
	}
	
	public String getID(){ return this.id; }

	public void setRelatedDocuments(Document relatedDocuments){
		this.relatedDocuments = relatedDocuments;
	}
	@Override
    public String toString(){
		return "Project: { id: " + this.id +
				" name: " + this.name + 
				" description: " + this.description +
				" Related Documents: " + this.relatedDocuments +
				" }";
	}

	

}
