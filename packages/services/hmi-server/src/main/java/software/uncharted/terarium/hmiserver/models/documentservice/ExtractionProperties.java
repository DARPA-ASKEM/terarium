package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)

public class ExtractionProperties implements Serializable {
	private String title;

	private String DOI;

	private String trustScore;

	@JsonProperty("abstract")
	private String abstractText;

	private String xddId;

	private String documentId;

	private String documentTitle;

	private String contentText;

	private Number indexInDocument;
	//TODO fixme from object
	private Object contentJSON;

	private String image;

	private String relevantSentences;

	private String sectionID;

	private String sectionTitle;

	private String caption;

	private Document documentBibjson;

}
