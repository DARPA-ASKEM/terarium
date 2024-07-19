package software.uncharted.terarium.hmiserver.models.dataservice.code;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TSModel
@Entity
public class CodeFile extends TerariumEntity {

	@Column(length = 512)
	private String fileName;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Dynamics dynamics;

	private ProgrammingLanguage language;

	@TSIgnore
	public CodeFile setFileNameAndProgrammingLanguage(final String fileName) {
		this.fileName = fileName;

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
		return this;
	}

	@Override
	public CodeFile clone() {
		final CodeFile clone = new CodeFile();
		clone.fileName = this.fileName;
		clone.language = this.language;
		if (this.dynamics != null) clone.dynamics = this.dynamics.clone();
		return clone;
	}
}
