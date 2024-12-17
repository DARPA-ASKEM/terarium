package software.uncharted.terarium.taskrunner.models.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRequest implements Serializable {

	private UUID id;
	private String script;
	private byte[] input;
	private int timeoutMinutes = 5;
	private Object additionalProperties;
	protected String userId;
	protected UUID projectId;
	protected String requestSHA256;
	protected String routingKey;
	protected boolean noCache = false;

	public TaskResponse createResponse(final TaskStatus status, final String stdout, final String stderr) {
		return new TaskResponse()
			.setId(id)
			.setStatus(status)
			.setScript(script)
			.setUserId(userId)
			.setProjectId(projectId)
			.setAdditionalProperties(additionalProperties)
			.setStdout(stdout)
			.setStderr(stderr)
			.setRoutingKey(routingKey)
			.setNoCache(noCache)
			.setRequestSHA256(requestSHA256);
	}
}
