package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

import java.io.Serial;
import java.io.Serializable;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class DataColumn extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = 892484869215158123L;

	private String id;

	private String name;

	private MetadataDataset dataset;
}
