package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import jakarta.persistence.Entity;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
@Entity
public class ModelConfiguration extends TerariumAsset {
	private UUID calibrationRunId;
	private UUID modelId;
	private Map<String, Semantic> values;
}
