package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Id {

	private int id;

	@JsonCreator
	public Id(@JsonProperty("id") int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
