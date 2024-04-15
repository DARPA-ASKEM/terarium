package software.uncharted.terarium.taskrunner.models.task;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
	private int timeoutMinutes = 30;
	private Object additionalProperties;

	public TaskResponse createResponse(final TaskStatus status, final String stdout, final String stderr) {
		return new TaskResponse()
				.setId(id)
				.setStatus(status)
				.setScript(script)
				.setAdditionalProperties(additionalProperties)
				.setStdout(stdout)
				.setStderr(stderr);
	}
}
