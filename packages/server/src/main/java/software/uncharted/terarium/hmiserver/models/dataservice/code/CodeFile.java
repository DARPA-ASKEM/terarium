package software.uncharted.terarium.hmiserver.models.dataservice.code;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class CodeFile {

	private ProgrammingLanguage language;

	@JdbcTypeCode(SqlTypes.JSON)
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

	@Override
	public CodeFile clone() {
		final CodeFile clone = new CodeFile();
		clone.language = this.language;
		clone.dynamics = this.dynamics.clone();
		return clone;
	}
}
