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
			case COMPLETE:
				return onCancelling(response);
			case CANCELLED:
				return onCancelled(response);
			case ERROR:
				return onFailure(response);
		}
		throw new IllegalArgumentException("Unknown status: " + response.getStatus());
	}
}
