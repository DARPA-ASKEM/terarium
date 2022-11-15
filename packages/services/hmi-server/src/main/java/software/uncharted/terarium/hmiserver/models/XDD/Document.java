package software.uncharted.terarium.hmiserver.models.XDD;

import java.util.Hashtable;

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

	@JsonbProperty("_abstract")
	@JsonAlias("abstract")
	public String _abstract;

	@JsonbProperty("author")
  public XDDArticleAuthor[] author;

	@JsonbProperty("identifier")
  public XDDArticleIdentifier[] identifier;

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
  public XDDArticleLink[] link;

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
