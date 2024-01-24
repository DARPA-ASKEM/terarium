package software.uncharted.terarium.taskrunner.models.task;

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
	private String taskKey;
	private byte[] input;
	private int timeoutMinutes = 30;
}
