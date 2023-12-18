package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

/**
 * The workflow data structure is not very well defined. It is also meant to
 * carry operations each with their own unique
 * representations. As such this is just a pass-thru class for the proxy. The UI
 * has it's own typinging definition that is
 * not generated.
 */
@Data
@Accessors(chain = true)
public class Workflow implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String name;

	private String description;

	private Object transform;

	private List<Object> nodes;

	private List<Object> edges;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp createdOn;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp deletedOn;
}
