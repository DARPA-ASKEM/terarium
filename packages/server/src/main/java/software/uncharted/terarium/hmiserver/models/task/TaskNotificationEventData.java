package software.uncharted.terarium.hmiserver.models.task;

import lombok.Value;
import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;

import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;

@Value
@Builder
@TSModel
public class TaskNotificationEventData implements Serializable {
	private UUID taskId;

	private TaskType taskType;

	private TaskStatus status;

	private String message;

	private String error;

	private Object data;

	public static TaskNotificationEventData createFrom(TaskResponse resp) {
		return TaskNotificationEventData.builder()
				.taskId(resp.getId())
				.taskType(resp.getType())
				.status(resp.getStatus())
				.message(resp.getStdout())
				.error(resp.getStderr())
				.data(resp.getAdditionalProperties())
				.build();
	}
}
