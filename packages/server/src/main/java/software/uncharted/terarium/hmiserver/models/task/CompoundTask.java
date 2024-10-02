package software.uncharted.terarium.hmiserver.models.task;

import java.util.List;
import lombok.Data;

/**
 * Represents a compound task that consists of a primary task and one or more secondary tasks.
 */
@Data
public class CompoundTask extends TaskRequest {

	/**
	 * Constructs a CompoundTask with a primary task and optional secondary tasks.
	 *
	 * @param primaryTask the primary task
	 * @param secondaryTasks the secondary tasks
	 */
	public CompoundTask(TaskRequest primaryTask, TaskRequest... secondaryTasks) {
		this.primaryTask = primaryTask;
		this.secondaryTasks = List.of(secondaryTasks);
	}

	/** The primary task of the compound task. */
	private TaskRequest primaryTask;

	/** The list of secondary tasks of the compound task. */
	private List<TaskRequest> secondaryTasks;
}
