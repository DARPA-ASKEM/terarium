package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.Date;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * XDD Document extraction representation
 */
@Data
@Accessors(chain = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Extraction implements Serializable {

	@JsonbProperty("ASKEM_CLASS")
	private String askemClass;

	@JsonbProperty("properties")
  private ExtractionProperties properties;

	@JsonbProperty("askem_id")
	private String askemId;

	@JsonbProperty("_xdd_created")
  private Date xddCreated;

	@JsonbProperty("_xdd_registrant")
  private Number xddRegistrant;
}

@Data
@Accessors(chain = true)
class ExtractionProperties implements Serializable {
	private String title;

	private String DOI;

	private String trustScore;

	@JsonbProperty("_abstract")
	@JsonAlias("abstract")
	private String abstractText;

	private String XDDID;

	private String documentID;

	private String documentTitle;

	private String contentText;

	private Number indexInDocument;

	private Map<String, Map<String, String>> contentJSON;

	private String image;

	private String relevantSentences;

	private String sectionID;

	private String sectionTitle;

	private String caption;
};
