package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
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
	private SimulationRequest executionPayload;

	@TSOptional
	private String description;

	@JsonAlias("result_files")
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ElementCollection
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
	private UUID workflowId; //TODO JOIN

	@OneToOne
	@JoinColumn(name = "workflowActual_id", nullable = true)
	@JsonBackReference
	private Workflow workflowActual;


	@JsonAlias("user_id")
	@TSOptional
	private String userId;

	@JsonAlias("project_id")
	@TSOptional
	//TODO JOIN?
	private UUID projectId;

	@Override
	public Simulation clone() {
		final Simulation clone = new Simulation();

		cloneSuperFields(clone);

		clone.setDescription(this.description); //done

		clone.setResultFiles(new ArrayList<>(this.resultFiles)); //done
		clone.setType(SimulationType.valueOf(this.type.name())); //done
		clone.setStatus(ProgressState.valueOf(this.status.name())); //done
		clone.setStatusMessage(this.statusMessage); //done
		clone.setStartTime(this.startTime != null ? new Timestamp(this.startTime.getTime()) : null); //done
		clone.setCompletedTime(this.completedTime != null ? new Timestamp(this.completedTime.getTime()) : null); //done
		clone.setEngine(SimulationEngine.valueOf(this.engine.name())); //done
		clone.setUserId(this.userId); // done
		clone.setExecutionPayload(this.executionPayload.clone()); //done
		clone.setWorkflowId(this.workflowId); //TODO
		clone.setProjectId(this.projectId); // TODO


		return clone;
	}

}
