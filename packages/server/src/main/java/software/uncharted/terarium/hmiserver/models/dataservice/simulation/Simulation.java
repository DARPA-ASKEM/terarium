package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.ProgressState;
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
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode executionPayload;

	@JsonAlias("result_files")
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<String> resultFiles;

	@Enumerated(EnumType.STRING)
	private SimulationType type;

	@Enumerated(EnumType.STRING)
	private ProgressState status;

	@TSOptional
	private Double progress = 0.0;

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

	@JsonAlias("user_id")
	@TSOptional
	@Column(length = 255)
	private String userId;

	@JsonAlias("project_id")
	@TSOptional
	private UUID projectId;

	@OneToMany(mappedBy = "simulation", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@OrderBy("createdOn DESC")
	@JsonManagedReference
	private List<SimulationUpdate> updates = new ArrayList<>();

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
		clone.setProjectId(this.projectId);

		for (final SimulationUpdate update : this.updates) {
			clone.getUpdates().add(update.clone(clone));
		}

		return clone;
	}
}
