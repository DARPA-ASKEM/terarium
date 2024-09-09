package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class Paper extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -2218015536823471410L;

	private String id;

	@JsonProperty("file_directory")
	private String fileDirectory;

	private String doi;

	@Override
	public Paper clone() {
		final Paper clone = (Paper) super.clone();
		clone.id = id;
		clone.fileDirectory = fileDirectory;
		clone.doi = doi;
		return clone;
	}
}
