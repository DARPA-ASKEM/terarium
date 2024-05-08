package software.uncharted.terarium.hmiserver.models.task;

import java.io.Serializable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;

@Value
@Builder
@TSModel
public class TaskNotificationEventData implements Serializable {
	private UUID taskId;

	private TaskStatus status;

	private String message;

	private String error;

	private Object data;

	public static TaskNotificationEventData createFrom(TaskResponse resp) {
		return TaskNotificationEventData.builder()
				.taskId(resp.getId())
				.status(resp.getStatus())
				.message(resp.getStdout())
				.error(resp.getStderr())
				.data(resp.getAdditionalProperties())
				.build();
	}
}
