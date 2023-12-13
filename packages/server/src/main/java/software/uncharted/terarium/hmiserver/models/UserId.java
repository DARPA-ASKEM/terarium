package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class UserId {

	private final long id;

	@JsonCreator
	public UserId(long id) {
		this.id = id;
	}

	@JsonValue
	public long toLong() {
		return id;
	}

	public String toString() {
		return String.valueOf(id);
	}
}
