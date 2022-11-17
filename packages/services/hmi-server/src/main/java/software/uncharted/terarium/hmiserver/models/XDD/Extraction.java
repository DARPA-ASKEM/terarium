package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.Date;
import java.util.Hashtable;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * XDD Document extraction representation
 */
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Extraction {

	@JsonbProperty("ASKEM_CLASS")
	public String ASKEM_CLASS;

	@JsonbProperty("properties")
  public ExtractionProperties properties;

	@JsonbProperty("askem_id")
	public String askem_id;

	@JsonbProperty("_xdd_created")
  public Date _xdd_created;

	@JsonbProperty("_xdd_registrant")
  public Number _xdd_registrant;
}

class ExtractionProperties {
	@JsonbProperty("title")
	public String title;

	@JsonbProperty("DOI")
	public String DOI;

	@JsonbProperty("trustScore")
	public String trustScore;

	@JsonbProperty("_abstract")
	@JsonAlias("abstract")
	public String _abstract;

	@JsonbProperty("XDDID")
	public String XDDID;

	@JsonbProperty("documentID")
	public String documentID;

	@JsonbProperty("documentTitle")
	public String documentTitle;

	@JsonbProperty("contentText")
	public String contentText;

	@JsonbProperty("indexInDocument")
	public Number indexInDocument;

	@JsonbProperty("contentJSON")
	public Hashtable<String, Hashtable<String, String>> contentJSON;

	@JsonbProperty("image")
	public String image;

	@JsonbProperty("relevantSentences")
	public String relevantSentences;

	@JsonbProperty("sectionID")
	public String sectionID;

	@JsonbProperty("sectionTitle")
	public String sectionTitle;

	@JsonbProperty("caption")
	public String caption;
};
