package software.uncharted.terarium.hmiserver.models.simulationservice;

import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Simulation implements {
	private String id;

	@JsonAlias("execution_payload")
	private Object executionPayload;

	private String type;
	private String status;
	private String engine;

	@JsonAlias("workflow_id")
	private String workflowId;
}

/*
{
  "id": "sciml-00000000-0000-0000-0010-f8d90a5e082b",
  "execution_payload": {
     "engine": "sciml",
     "model_config_id": "12345678-..."
  },
  "type": "calibration",
  "status": "queued",
  "engine": "sciml",
  "workflow_id": "fake"
}
*/
