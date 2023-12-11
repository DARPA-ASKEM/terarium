package software.uncharted.terarium.hmiserver.models.dataservice.code;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.dataservice.code.CodeFile;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.Map;
import java.time.Instant;

@Data
@Accessors(chain = true)
@TSModel
public class Code {

	/* The id of the code. */
	@TSOptional
	private String id;

	/* Timestamp of creation */
	@TSOptional
	private Instant timestamp;

	/* The name of the code. */
	private String name;

	/* The description of the code. */
	private String description;

	/* Files that contain dynamics */
	@TSOptional
	private Map<String, CodeFile> files;

	/* The optional URL for where this code came from */
	@TSOptional
	@JsonAlias("repo_url")
	private String repoUrl;

	/* The optional metadata for this code */
	@TSOptional
	private JsonNode metadata;


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
}
