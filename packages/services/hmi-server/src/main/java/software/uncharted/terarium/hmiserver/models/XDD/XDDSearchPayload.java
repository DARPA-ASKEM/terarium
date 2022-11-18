package software.uncharted.terarium.hmiserver.models.xdd;

import javax.json.bind.annotation.JsonbProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDSearchPayload implements Serializable {
	private String doi;

	private String term;

	private String title;

	private String dataset; // dataset/collection name

	// Extraction-specific fields
	private String type;

	@JsonbProperty("ignore_bytes")
	private Boolean ignoreBytes;
};
