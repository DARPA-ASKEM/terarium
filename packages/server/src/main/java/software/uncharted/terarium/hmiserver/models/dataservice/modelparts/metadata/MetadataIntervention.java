package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class MetadataIntervention extends SupportAdditionalProperties {
	private String name;

	private String type;

	private String target;

	private String value;

	@JsonAlias("start_time")
	private String startTime;
}
