package software.uncharted.terarium.hmiserver.models.dataservice.code;

import software.uncharted.terarium.hmiserver.models.dataservice.code.Dynamics;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code.ProgrammingLanguage;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class CodeFile {
    private ProgrammingLanguage language;
    private Dynamics dynamics;

    @TSIgnore
    public void setProgrammingLanguageFromFileName(String fileName) {
        // Given the extension of a file, return the programming language
        String[] parts = fileName.split("\\.");
        String fileExtension = parts.length > 0 ? parts[parts.length - 1] : "";
    
        ProgrammingLanguage language = null;
        switch (fileExtension) {
            case "py":
                language = ProgrammingLanguage.PYTHON;
                break;
            case "jl":
                language = ProgrammingLanguage.Julia;
                break;
            case "r":
                language = ProgrammingLanguage.R;
                break;
            case "zip":
                language = ProgrammingLanguage.ZIP;
                break;
            default:
                language = null; // TODO: Do we need an "unknown" language?
        }

        this.setLanguage(language);
    }
}