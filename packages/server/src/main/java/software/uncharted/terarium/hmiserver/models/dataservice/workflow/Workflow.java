package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * The workflow data structure is not very well defined. It is also meant to
 * carry operations each with their own unique
 * representations. As such this is just a pass-thru class for the proxy. The UI
 * has it's own typinging definition that is
 * not generated.
 */
@Data
@Accessors(chain = true)
@TSModel
public class Workflow implements Serializable {

	@Serial
	private static final long serialVersionUID = -1565930053830366145L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String name;

	private String description;

	private Object transform;

	private List<Object> nodes;

	private List<Object> edges;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp createdOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp deletedOn;
}
