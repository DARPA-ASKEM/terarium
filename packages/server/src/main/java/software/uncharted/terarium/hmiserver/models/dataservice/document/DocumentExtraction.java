package software.uncharted.terarium.hmiserver.models.dataservice.document;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentExtraction implements Serializable {

	@JsonAlias("file_name")
	private String fileName;

	@JsonAlias("asset_type")
	private ExtractionAssetType assetType;

	private Map<String, Object> metadata;
}
