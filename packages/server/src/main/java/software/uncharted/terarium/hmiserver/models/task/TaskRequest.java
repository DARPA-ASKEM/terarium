package software.uncharted.terarium.hmiserver.models.task;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class TaskRequest implements Serializable {
	private UUID id;
	private String script;
	private byte[] input;
	private int timeoutMinutes = 30;

	// Sometimes we have context specific variables what we want to associate with a
	// request but aren't actually used by the task on the other side but are
	// necessary for the response to be processed correctly. This property is used
	// to put those variables in. They aren't used by the taskrunner or the task
	// itself, but are passed through to every response.
	private Object additionalProperties;

	public void setInput(byte[] bytes) throws JsonProcessingException {
		input = bytes;
	}

	public void setInput(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		input = mapper.writeValueAsBytes(obj);
	}

	public TaskResponse createResponse(TaskStatus status) {
		return new TaskResponse()
				.setId(id)
				.setStatus(status)
				.setScript(script)
				.setAdditionalProperties(additionalProperties);
	}

	public <T> T getAdditionalProperties(Class<T> type) {
		if (type.isInstance(additionalProperties)) {
			return type.cast(additionalProperties);
		} else {
			throw new IllegalArgumentException("Value is not of type " + type.getName());
		}
	}
}
