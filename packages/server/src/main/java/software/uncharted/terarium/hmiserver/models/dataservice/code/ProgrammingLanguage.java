package software.uncharted.terarium.hmiserver.models.dataservice.code;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProgrammingLanguage {
	PYTHON("python"),
	R("r"),
	Julia("julia"),
	ZIP("zip");

	public final String language;

	ProgrammingLanguage(final String language) {
		this.language = language;
	}

	@Override
	@JsonValue
	public String toString() {
		return language;
	}

	public static ProgrammingLanguage fromString(final String language) {
		return ProgrammingLanguage.valueOf(language.toUpperCase());
	}
}
