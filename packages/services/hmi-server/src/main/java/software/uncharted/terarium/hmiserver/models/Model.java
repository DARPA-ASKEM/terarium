package software.uncharted.terarium.hmiserver.models;

import java.util.Date;
import java.util.Hashtable;
import java.util.Optional;

import javax.json.bind.annotation.JsonbProperty;
// import java.util.UUID;

public class Model {

	@JsonbProperty("id")
	public String id; // UUID

	@JsonbProperty("name")
  public String name;

	@JsonbProperty("description")
	public String description;

	//

	@JsonbProperty("source")
  public String source;

	@JsonbProperty("status")
  public String status;

	@JsonbProperty("category")
  public String category;

	@JsonbProperty("type")
  public String type;

	//

	@JsonbProperty("framework")
  public String framework;

	@JsonbProperty("timestamp")
  public Date timestamp;

	@JsonbProperty("parameters")
  public Hashtable<String, String> parameters;

	@JsonbProperty("concept")
  public String concept;

	@JsonbProperty("content")
  public ModelContent content;

}

class ModelContent {
	@JsonbProperty("S")
  public Hashtable<String, String>[] S;

	@JsonbProperty("T")
  public Hashtable<String, Optional<String>>[] T;

	@JsonbProperty("I")
  public Hashtable<String, Number>[] I;

	@JsonbProperty("O")
  public Hashtable<String, Number>[] O;
}
