package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ExtractionProperties implements Serializable {
	private String title;

	private String DOI;

	private String trustScore;

	@JsonAlias("abstract")
	private String abstractText;

	private String xddId;

	private String documentId;

	private String documentTitle;

	private String contentText;

	private Number indexInDocument;

	private Map<String, Map<String, String>> contentJSON;

	private String image;

	private String relevantSentences;

	private String sectionID;

	private String sectionTitle;

	private String caption;

	private Document documentBibjson;

	@JsonbProperty("_abstract")
	public void setAbstract(String abstractText) {
		this.abstractText = abstractText;
	}
}
