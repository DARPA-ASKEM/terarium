package software.uncharted.terarium.hmiserver.models;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class Model {
	public String id;

  public String name;

	public String description;

  public String framework;

  public Date timestamp;

  public Map<String, String> parameters;

  public String concept;

  public ModelContent content;
}

class ModelContent {
  public Map<String, String>[] S;

  public Map<String, Optional<String>>[] T;

  public Map<String, Number>[] I;

  public Map<String, Number>[] O;
}
