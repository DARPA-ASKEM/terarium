package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentExtraction implements Serializable {

    @JsonAlias("file_name")
	private String fileName;

    @JsonAlias("asset_type")
	private String assetType;

	private Object metadata;
}
