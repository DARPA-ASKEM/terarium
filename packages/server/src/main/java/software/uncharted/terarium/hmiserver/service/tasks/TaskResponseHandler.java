package software.uncharted.terarium.hmiserver.service.tasks;

import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

public abstract class TaskResponseHandler {

	/**
	 * Without tracking "on behalf of" user and corresponding project to check for permissions, assume the user had
	 * permissions at the time of the Task's creation
	 */
	protected final Schema.Permission ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER = Schema.Permission.WRITE;

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
			case CANCELLING:
				return onCancelling(response);
			case CANCELLED:
				return onCancelled(response);
			case SUCCESS:
				return onSuccess(response);
			case FAILED:
				return onFailure(response);
		}
		throw new IllegalArgumentException("Unknown status: " + response.getStatus());
	}
}
