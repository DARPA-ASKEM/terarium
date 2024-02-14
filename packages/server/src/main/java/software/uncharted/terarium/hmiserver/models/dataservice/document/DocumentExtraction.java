package software.uncharted.terarium.hmiserver.models.dataservice.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

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
}
