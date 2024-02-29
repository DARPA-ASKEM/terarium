package software.uncharted.terarium.hmiserver.service.tasks;

import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class TaskResponseHandler {
	private final Map<TaskStatus, Consumer<TaskResponse>> responseHandlers = new ConcurrentHashMap<>();

	public abstract String getName();

	public void onRunning(final Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.RUNNING, callback);
	}

	public void onCancelling(final Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.CANCELLING, callback);
	}

	public void onCancelled(final Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.CANCELLED, callback);
	}

	public void onSuccess(final Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.SUCCESS, callback);
	}

	public void onFailure(final Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.FAILED, callback);
	}

	public void handle(final TaskResponse response) {
		if (!responseHandlers.containsKey(response.getStatus())) {
			return;
		}
		responseHandlers.get(response.getStatus()).accept(response);
	}

}
