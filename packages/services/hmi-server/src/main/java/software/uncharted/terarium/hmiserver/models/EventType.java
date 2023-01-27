package software.uncharted.terarium.hmiserver.models;

import lombok.Getter;

public enum EventType {
	SEARCH(true);

	EventType(boolean persistent) {
		this.persistent = persistent;
	}

	EventType() {
		this.persistent = false;
	}

	@Getter
	final boolean persistent;
}
