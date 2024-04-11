package software.uncharted.terarium.hmiserver.models.dataservice.code;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class Code extends TerariumAsset {

  @Serial private static final long serialVersionUID = 3041175096070970227L;

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
