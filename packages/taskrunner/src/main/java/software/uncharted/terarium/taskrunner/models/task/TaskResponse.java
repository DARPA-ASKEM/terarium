package software.uncharted.terarium.taskrunner.models.task;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class TaskResponse {
	private UUID id;
	private TaskStatus status;
	private byte[] output;
}
