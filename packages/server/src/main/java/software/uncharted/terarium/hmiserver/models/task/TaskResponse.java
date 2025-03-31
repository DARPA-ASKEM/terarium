package software.uncharted.terarium.hmiserver.models.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Accessors(chain = true)
@NoArgsConstructor
@Data
@TSModel
public class TaskResponse implements Serializable {

	private UUID id;
	private String script;
	private TaskStatus status;
	private byte[] output;
	protected String userId;

	private UUID projectId;

	// The value of this will be whatever it was set to on the TaskRequest.
	private Object additionalProperties;

	private String stdout;
	private String stderr;
	private String requestSHA256;
	private String routingKey;
	private boolean useCache = true;

	public <T> T getAdditionalProperties(final Class<T> type) throws JsonProcessingException {
		if (additionalProperties == null) {
			return null;
		}
		final ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(objectMapper.writeValueAsString(additionalProperties), type);
	}
}
