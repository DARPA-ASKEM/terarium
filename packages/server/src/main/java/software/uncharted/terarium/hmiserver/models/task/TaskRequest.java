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
public class TaskRequest {
	private UUID id;
	private String taskKey;
	private byte[] input;
}
