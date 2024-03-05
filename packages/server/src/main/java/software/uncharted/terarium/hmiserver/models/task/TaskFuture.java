package software.uncharted.terarium.hmiserver.models.task;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class TaskFuture implements Serializable {
	private UUID id;
	private TaskResponse latestResponse;
	private CompletableFuture<TaskResponse> completeFuture;
}
