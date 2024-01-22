package software.uncharted.terarium.hmiserver.models.task;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Accessors(chain = true)
@NoArgsConstructor
@Data
@TSModel
public class TaskResponse {
	private UUID id;
	private TaskStatus status;
	private byte[] output;
}
