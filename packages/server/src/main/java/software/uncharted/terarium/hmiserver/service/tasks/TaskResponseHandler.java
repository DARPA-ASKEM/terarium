package software.uncharted.terarium.hmiserver.service.tasks;

import software.uncharted.terarium.hmiserver.models.task.TaskResponse;

public abstract class TaskResponseHandler {

	public abstract String getName();

	public TaskResponse onQueued(final TaskResponse response) {
		return response;
	}

	public TaskResponse onRunning(final TaskResponse response) {
		return response;
	}

	public TaskResponse onCancelling(final TaskResponse response) {
		return response;
	}

	public TaskResponse onCancelled(final TaskResponse response) {
		return response;
	}

	public TaskResponse onSuccess(final TaskResponse response) {
		return response;
	}

	public TaskResponse onFailure(final TaskResponse response) {
		return response;
	}

	public TaskResponse handle(final TaskResponse response) {
		switch (response.getStatus()) {
			case QUEUED:
				return onQueued(response);
			case RUNNING:
				return onRunning(response);
			case SUCCESS:
				return onCancelling(response);
			case CANCELLED:
			case CANCELLING:
				return onCancelled(response);
			case FAILED:
				return onFailure(response);
		}
		throw new IllegalArgumentException("Unknown status: " + response.getStatus());
	}
}
