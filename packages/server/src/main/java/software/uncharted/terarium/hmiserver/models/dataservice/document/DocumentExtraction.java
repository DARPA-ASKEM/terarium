package software.uncharted.terarium.hmiserver.models.dataservice.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentExtraction implements Serializable {

	@Serial
	private static final long serialVersionUID = 7781295818378531195L;

	@JsonAlias("file_name")
	private String fileName;

	@JsonAlias("asset_type")
	private ExtractionAssetType assetType;

	private Map<String, Object> metadata;

	@Override
	public DocumentExtraction clone() {
		final DocumentExtraction clone = new DocumentExtraction();

		clone.fileName = this.fileName;
		clone.assetType = this.assetType;
		if (this.metadata != null) {
			clone.metadata = new HashMap<>();
			for (final String key : this.metadata.keySet()) {
				// I don't like that this is an "object" because it doesn't clone nicely...
				clone.metadata.put(key, this.metadata.get(key));
			}
		}

		return clone;
	}
}
