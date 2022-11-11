package software.uncharted.terarium.hmiserver.models;

import javax.json.bind.annotation.JsonbProperty;
import java.util.UUID;

public class Model {

	@JsonbProperty("id")
	public UUID id;

	@JsonbProperty("name")
  public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("source")
  public String source;

	@JsonbProperty("status")
  public String status;

	@JsonbProperty("category")
  public String category;

	@JsonbProperty("type")
  public String type;
}
