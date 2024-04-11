package software.uncharted.terarium.hmiserver.models.dataservice.code;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code.ProgrammingLanguage;

@Data
@Accessors(chain = true)
@TSModel
public class CodeFile {
  private ProgrammingLanguage language;
  private Dynamics dynamics;

  @TSIgnore
  public void setProgrammingLanguageFromFileName(final String fileName) {
    // Given the extension of a file, return the programming language
    final String[] parts = fileName.split("\\.");
    final String fileExtension = parts.length > 0 ? parts[parts.length - 1] : "";

    final ProgrammingLanguage language =
        switch (fileExtension) {
          case "py" -> ProgrammingLanguage.PYTHON;
          case "jl" -> ProgrammingLanguage.Julia;
          case "r" -> ProgrammingLanguage.R;
          case "zip" -> ProgrammingLanguage.ZIP;
          default -> null; // TODO: Do we need an "unknown" language?
        };

    this.setLanguage(language);
  }
}
