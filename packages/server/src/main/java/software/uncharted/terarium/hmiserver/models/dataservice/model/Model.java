package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelSemantics;

@Data
@Accessors(chain = true)
@TSModel
public class Model implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private ModelHeader header;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp createdOn;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp deletedOn;

	@TSOptional
	private String userId;

	private Map<String, Object> model;

	@TSOptional
	private Object properties;

	@TSOptional
	private ModelSemantics semantics;

	@TSOptional
	private ModelMetadata metadata;
}
