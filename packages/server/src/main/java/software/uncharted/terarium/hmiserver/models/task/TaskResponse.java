package software.uncharted.terarium.hmiserver.models.task;

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

	// The value of this will be whatever it was set to on the TaskRequest.
	private Object additionalProperties;

	public <T> T getAdditionalProperties(Class<T> type) {
		if (type.isInstance(additionalProperties)) {
			return type.cast(additionalProperties);
		} else {
			throw new IllegalArgumentException("Value is not of type " + type.getName());
		}
	}
}
