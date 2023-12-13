package software.uncharted.terarium.hmiserver.models.dataservice.code;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.sql.Timestamp;
import java.util.Map;
import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(chain = true)
@TSModel
public class Code {

	/* The id of the code. */
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@UpdateTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;

	/* The name of the code. */
	@Schema(defaultValue = "Default Name")
	private String name;

	/* The description of the code. */
	@Schema(defaultValue = "Default Description")
	private String description;

	/* Files that contain dynamics */
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "{}")
	private Map<String, CodeFile> files;

	/* The optional URL for where this code came from */
	@TSOptional
	@JsonAlias("repo_url")
	private String repoUrl;

	/* The optional metadata for this code */
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "{}")
	private Map<String, String> metadata;


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
