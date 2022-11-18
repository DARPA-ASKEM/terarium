package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.List;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * XDD Document representation
 */
@Data
@Accessors(chain = true)
public class Document implements Serializable {

	@JsonbProperty("_gddid")
	private String gddid;

  private String title;

	@JsonbProperty("_abstract")
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

	@JsonbProperty("known_terms")
  private Map<String, List<String>> knownTerms;

}
