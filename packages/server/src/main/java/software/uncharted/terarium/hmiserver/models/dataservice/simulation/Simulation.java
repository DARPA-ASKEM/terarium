package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@TSModel
public class Simulation extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 5467224100686908152L;

	@JsonAlias("execution_payload")
	@Column(columnDefinition = "text")
	private JsonNode executionPayload;

	@JsonAlias("result_files")
	@TSOptional
	@Column(length = 1024)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ElementCollection
	private List<String> resultFiles;

	@Enumerated(EnumType.STRING)
	private SimulationType type;

	@Enumerated(EnumType.STRING)
	private ProgressState status;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "text")
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
	@Column(length = 255)
	private String userId;

	@JsonAlias("project_id")
	@TSOptional
	private UUID projectId; // TODO this can probably be joined to the project table soon?

	@Override
	public Simulation clone() {
		final Simulation clone = new Simulation();

		cloneSuperFields(clone);

		clone.setResultFiles(new ArrayList<>(this.resultFiles));
		clone.setType(SimulationType.valueOf(this.type.name()));
		clone.setStatus(ProgressState.valueOf(this.status.name()));
		clone.setStatusMessage(this.statusMessage);
		clone.setStartTime(this.startTime != null ? new Timestamp(this.startTime.getTime()) : null);
		clone.setCompletedTime(this.completedTime != null ? new Timestamp(this.completedTime.getTime()) : null);
		clone.setEngine(SimulationEngine.valueOf(this.engine.name()));
		clone.setUserId(this.userId);
		clone.setExecutionPayload(this.executionPayload.deepCopy());
		clone.setProjectId(this.projectId); // TODO

		return clone;
	}
}
