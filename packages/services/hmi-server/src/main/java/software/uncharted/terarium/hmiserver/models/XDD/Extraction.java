package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.Date;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * XDD Document extraction representation
 */
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Extraction {

	@JsonbProperty("ASKEM_CLASS")
	public String askemClass;

	@JsonbProperty("properties")
  public ExtractionProperties properties;

	@JsonbProperty("askem_id")
	public String askemId;

	@JsonbProperty("_xdd_created")
  public Date xddCreated;

	@JsonbProperty("_xdd_registrant")
  public Number xddRegistrant;
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
	public String abstractText;

	public String XDDID;

	public String documentID;

	public String documentTitle;

	public String contentText;

	public Number indexInDocument;

	public Map<String, Map<String, String>> contentJSON;

	@JsonbProperty("image")
	public String image;

	public String relevantSentences;

	public String sectionID;

	public String sectionTitle;

	public String caption;
};
