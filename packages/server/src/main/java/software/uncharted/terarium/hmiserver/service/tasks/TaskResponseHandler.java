package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

public abstract class TaskResponseHandler {
	private String name;

	public TaskResponseHandler(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	private Map<TaskStatus, Consumer<TaskResponse>> responseHandlers = new ConcurrentHashMap<>();

	public void onRunning(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.RUNNING, callback);
	}

	public void onCancelling(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.CANCELLING, callback);
	}

	public void onCancelled(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.CANCELLED, callback);
	}

	public void onSuccess(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.SUCCESS, callback);
	}

	public void onFailure(Consumer<TaskResponse> callback) {
		responseHandlers.put(TaskStatus.FAILED, callback);
	}

	public void handle(TaskResponse response) {
		if (!responseHandlers.containsKey(response.getStatus())) {
			return;
		}
		responseHandlers.get(response.getStatus()).accept(response);
	}

}
