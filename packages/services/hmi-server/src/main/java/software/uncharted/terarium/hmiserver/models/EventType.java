package software.uncharted.terarium.hmiserver.models;

import lombok.Getter;

public enum EventType {
	SEARCH(true),
	EVALUATION_SCENARIO(true),
	ROUTE_TIMING(true),
	PROXY_TIMING(true);

	EventType(boolean persistent) {
		this.persistent = persistent;
	}

	EventType() {
		this.persistent = false;
	}

	@Getter
	final boolean persistent;
}
