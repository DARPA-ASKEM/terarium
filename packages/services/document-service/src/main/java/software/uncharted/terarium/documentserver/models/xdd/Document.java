package software.uncharted.terarium.documentserver.models.xdd;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * XDD Document representation
 */
@Data
@Accessors(chain = true)
public class Document implements Serializable {

	private String gddId;

	private String title;

	@JsonAlias("abstract")
	private String abstractText;

	private String journal;

	private String type;

	private String number;

	private String pages;

	private String publisher;

	private String volume;

	private String year;

	private List<Map<String, String>> link;

	private List<Map<String, String>> author;

	private List<Map<String, String>> identifier;
	private Map<String, List<String>> knownTerms;
	private List<String> highlight;

	private List<Document> relatedDocuments;

	private List<Extraction> relatedExtractions;
	private KnownEntities knownEntities;

	@JsonbProperty("_gddid")
	public void setID(String id) {
		this.gddId = id;
	}

	public String getID(){ return this.gddId; }

	@JsonbProperty("_abstract")
	public void setAbstract(String abstractText) {
		this.abstractText = abstractText;
	}

	@JsonbProperty("known_terms")
	public void setKnownTerms(Map<String, List<String>> knownTerms) {
		this.knownTerms = knownTerms;
	}

	@JsonbProperty("_highlight")
	public void setKnownTerms(List<String> highlight) {
		this.highlight = highlight;
	}

	@JsonbProperty("known_entities")
	public void setKnownEntities(KnownEntities knownEntities) {
		this.knownEntities = knownEntities;
	}

	public Document(String gddId, String title, String abstractText, String journal, String publisher, List<Map<String, String>> author, List<Map<String, String>> identifier){
		this.gddId = gddId;
		this.title = title;
		this.abstractText = abstractText;
		this.journal = journal;
		this.publisher = publisher;
		this.author = author;
		this.identifier = identifier; 
	}
	
	public Document(){} //Default constructor for @Data

	@Override
    public String toString(){
		return "Document: { gddId: " + this.gddId +
				" title: " + this.title + 
				" abstractText: " + this.abstractText +
				" journal: " + this.journal +
				" }";
	}

}

