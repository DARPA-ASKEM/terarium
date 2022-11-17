package software.uncharted.terarium.hmiserver.models.XDD;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * XDD Document representation
 */
public class Document {

	@JsonbProperty("_gddid")
	public String _gddid;

	@JsonbProperty("title")
  public String title;

	// FIXME: can we have the field name without the "_" prefix?
	@JsonbProperty("_abstract")
	@JsonAlias("abstract")
	public String _abstract;

	@JsonbProperty("journal")
  public String journal;

	@JsonbProperty("type")
  public String type;

	@JsonbProperty("number")
  public String number;

	@JsonbProperty("pages")
  public String pages;

	@JsonbProperty("publisher")
  public String publisher;

	@JsonbProperty("volume")
  public String volume;

	@JsonbProperty("year")
  public String year;

	@JsonbProperty("link")
  public List<HashMap<String, String>> link;

	@JsonbProperty("author")
  public List<HashMap<String, String>> author;

	@JsonbProperty("identifier")
  public List<HashMap<String, String>> identifier;

	@JsonbProperty("known_terms")
  public Hashtable<String, String[]> known_terms;

}
