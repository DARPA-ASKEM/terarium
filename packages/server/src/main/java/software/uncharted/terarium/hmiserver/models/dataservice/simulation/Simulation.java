package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.utils.hibernate.JpaConverterJson;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@TSModel
public class Simulation extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 5467224100686908152L;

	@JsonAlias("execution_payload")
	@Convert(converter = JpaConverterJson.class)
	private Object executionPayload;

	@TSOptional
	private String description;

	@JsonAlias("result_files")
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private List<String> resultFiles;

	@Enumerated(EnumType.STRING)
	private SimulationType type;

	@Enumerated(EnumType.STRING)
	private ProgressState status;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String statusMessage;

	@JsonAlias("start_time")
	@TSOptional
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.S")
	private Timestamp startTime;

	@JsonAlias("completed_time")
	@TSOptional
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.S")
	private Timestamp completedTime;

	@Enumerated(EnumType.STRING)
	private SimulationEngine engine;

	@JsonAlias("workflow_id")
	private UUID workflowId;

	@JsonAlias("user_id")
	@TSOptional
	private String userId;

	@JsonAlias("project_id")
	@TSOptional
	private UUID projectId;

}
