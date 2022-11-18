package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.List;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * XDD Document representation
 */
public class Document {

	@JsonbProperty("_gddid")
	public String gddid;

  public String title;

	@JsonbProperty("_abstract")
	@JsonAlias("abstract")
	public String abstractText;

  public String journal;

  public String type;

  public String number;

  public String pages;

  public String publisher;

  public String volume;

  public String year;

  public List<Map<String, String>> link;

  public List<Map<String, String>> author;

  public List<Map<String, String>> identifier;

	@JsonbProperty("known_terms")
  public Map<String, List<String>> knownTerms;

}
