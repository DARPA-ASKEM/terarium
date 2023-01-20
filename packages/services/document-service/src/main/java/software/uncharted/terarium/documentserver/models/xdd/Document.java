package software.uncharted.terarium.documentserver.models.xdd;

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

	@JsonbProperty("abstract")
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

	private List<Map<String, String>> citationList;

	public String getID() {
		return this.gddId;
	}

	@JsonbProperty("known_terms")
	public void setKnownTerms(Map<String, List<String>> knownTerms) {
		this.knownTerms = knownTerms;
	}

	@JsonbProperty("_highlight")
	public void setHighlight
		(List<String> highlight) {
		this.highlight = highlight;
	}

	@JsonbProperty("known_entities")
	public void setKnownEntities(KnownEntities knownEntities) {
		this.knownEntities = knownEntities;
	}

	@JsonbProperty("citation_list")
	public void setCitationList(List<Map<String, String>> v) {
		this.citationList = v;
	}

	public Document() {
	} //Default constructor for @Data

}

