package software.uncharted.terarium.hmiserver.models;

import lombok.Getter;

public enum EventType {
	SEARCH(true),
	EVALUATION_SCENARIO(true),
	ROUTE_TIMING(true),
	PROXY_TIMING(true);
	ADD_RESOURCES_TO_PROJECT(true),
	EXTRACT_MODEL(true),
	PERSIST_MODEL(true),
	TRANSFORM_PROMPT(true),
	ADD_CODE_CELL(true),
	RUN_SIMULATION(true),
	RUN_CALIBRATE(true),
	GITHUB_IMPORT(true),
	EVALUATION_SCENARIO(false),
	ROUTE_TIMING(false);

	EventType(boolean persistent) {
		this.persistent = persistent;
	}

	EventType() {
		this.persistent = false;
	}

	@Getter
	final boolean persistent;
}
