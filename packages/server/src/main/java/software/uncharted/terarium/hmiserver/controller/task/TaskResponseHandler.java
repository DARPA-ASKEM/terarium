package software.uncharted.terarium.hmiserver.controller.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

public class TaskResponseHandler {

	private Map<TaskStatus, Consumer<TaskResponse>> responseHandlers = new ConcurrentHashMap<>();

	public void onRunning(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.QUEUED, callback);
	}

	public void onCancelling(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.QUEUED, callback);
	}

	public void onCancelled(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.QUEUED, callback);
	}

	public void onSuccess(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.QUEUED, callback);
	}

	public void onFailure(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.QUEUED, callback);
	}

	public void handle(TaskResponse response) {
		if (!responseHandlers.containsKey(response.getStatus())) {
			return;
		}
		responseHandlers.get(response.getStatus()).accept(response);
	}

}
