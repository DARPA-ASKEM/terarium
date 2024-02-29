package software.uncharted.terarium.hmiserver.service.tasks;

import software.uncharted.terarium.hmiserver.models.task.TaskResponse;

public abstract class TaskResponseHandler {
	public abstract String getName();

	public void onQueued(TaskResponse response) {
	}

	public void onRunning(TaskResponse response) {
	}

	public void onCancelling(TaskResponse response) {
	}

	public void onCancelled(TaskResponse response) {
	}

	public void onSuccess(TaskResponse response) {
	}

	public void onFailure(TaskResponse response) {
	}

	public void handle(TaskResponse response) {
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
