package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationParams;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@Accessors(chain = true)
@TSModel
// Used as the TDS Simulations object
public class Simulation {
	@TSOptional
	private String id;
	@JsonAlias("execution_payload")
	private Object executionPayload;
	@JsonAlias("result_files")
	private List<String> resultFiles;
	private String type;
	private String status;
	@JsonAlias("start_time")
	private LocalDateTime startTime;
	@JsonAlias("completed_time")
	private LocalDateTime completedTime;
	private String engine;
	@JsonAlias("workflow_id")
	private String workflowId;
	@JsonAlias("user_id")
	private Integer userId;
	@JsonAlias("project_id")
	private Integer projectId;
}
