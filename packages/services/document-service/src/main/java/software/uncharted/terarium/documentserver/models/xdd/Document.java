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

	@JsonbProperty("gddid")
	private String gddId;

	private String title;

	@JsonbProperty("abstract")
	// @JsonAlias("abstractText")
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

	@JsonbProperty("known_terms")
	private Map<String, List<String>> knownTerms;

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

	public String getID(){ return this.gddId; }
}
