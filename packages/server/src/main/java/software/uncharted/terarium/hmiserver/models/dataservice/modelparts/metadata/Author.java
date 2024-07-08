package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
@Data
@AMRSchemaType
public class Author {

	private String name;
}
