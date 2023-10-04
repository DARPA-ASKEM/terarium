package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
// Used as the TDS Simulations object
public class Simulation {
	private String id;
	@JsonAlias("execution_payload")
	private Object executionPayload;

	@TSOptional
	private String name;

	@TSOptional
	private String description;

	@JsonAlias("result_files")
	@TSOptional
	private List<String> resultFiles;
	private String type;
	private String status;

	@JsonAlias("start_time")
	@TSOptional
	private String startTime;

	@JsonAlias("completed_time")
	@TSOptional
	private String completedTime;
	private String engine;

	@JsonAlias("workflow_id")
	private String workflowId;

	@JsonAlias("user_id")
	@TSOptional
	private Integer userId;

	@JsonAlias("project_id")
	@TSOptional
	private Integer projectId;

}
