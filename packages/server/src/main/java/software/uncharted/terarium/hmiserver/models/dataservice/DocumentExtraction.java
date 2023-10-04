package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentExtraction implements Serializable {

	private String file_name;

	private String asset_type;

	private Object metadata;
}
