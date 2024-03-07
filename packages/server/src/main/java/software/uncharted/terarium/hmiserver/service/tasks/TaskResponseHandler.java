package software.uncharted.terarium.hmiserver.service.tasks;

import software.uncharted.terarium.hmiserver.models.task.TaskResponse;

public abstract class TaskResponseHandler {

	public abstract String getName();

	public void onQueued(final TaskResponse response) {
	}

	public void onRunning(final TaskResponse response) {
	}

	public void onCancelling(final TaskResponse response) {
	}

	public void onCancelled(final TaskResponse response) {
	}

	public void onSuccess(final TaskResponse response) {
	}

	public void onFailure(final TaskResponse response) {
	}

	public void handle(final TaskResponse response) {
		switch (response.getStatus()) {
			case QUEUED:
				onQueued(response);
				break;
			case RUNNING:
				onRunning(response);
				break;
			case CANCELLING:
				onCancelling(response);
				break;
			case CANCELLED:
				onCancelled(response);
				break;
			case SUCCESS:
				onSuccess(response);
				break;
			case FAILED:
				onFailure(response);
				break;
		}
	}

}
