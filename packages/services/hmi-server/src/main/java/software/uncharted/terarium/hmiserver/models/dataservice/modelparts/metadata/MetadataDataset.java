package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class MetadataDataset {
	private String id;
	private String name;
	private String metadata;
}

