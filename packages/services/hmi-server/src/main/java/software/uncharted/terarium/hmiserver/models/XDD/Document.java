package software.uncharted.terarium.hmiserver.models.XDD;

import java.util.Hashtable;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

	// FIXME: cannot use actual type or the rest-client would throw an error during the mapping
	@JsonbProperty("link")
  public Object link; // XDDArticleLink[]

	// FIXME: cannot use actual type or the rest-client would throw an error during the mapping
	@JsonbProperty("author")
	@JsonIgnore
  public Object author; // XDDArticleAuthor[]

	// FIXME: cannot use actual type or the rest-client would throw an error during the mapping
	@JsonbProperty("identifier")
  public Object identifier; // XDDArticleIdentifier[]

	@JsonbProperty("known_terms")
  public Hashtable<String, String[]> known_terms;

}

class XDDArticleAuthor {
	@JsonbProperty("name")
	public String name;
};

class XDDArticleIdentifier {
	@JsonbProperty("type")
	public String type;

	@JsonbProperty("id")
	public String id;
};

class XDDArticleLink {
	@JsonbProperty("type")
	public String type;

	@JsonbProperty("url")
	public String url;
};
