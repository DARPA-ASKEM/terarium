package software.uncharted.terarium.hmiserver.models.task;

import java.io.Serializable;
import java.util.UUID;

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
	private Object additionalProperties;

	public <T> T getAdditionalProperties(Class<T> type) {
		if (type.isInstance(additionalProperties)) {
			return type.cast(additionalProperties);
		} else {
			throw new IllegalArgumentException("Value is not of type " + type.getName());
		}
	}
}
