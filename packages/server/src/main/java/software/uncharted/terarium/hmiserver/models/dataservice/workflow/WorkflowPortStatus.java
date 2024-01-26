package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

public enum WorkflowPortStatus {
	CONNECTED("connected"),
	NOT_CONNECTED("not connected");

	private final String status;

	WorkflowPortStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return status;
	}
}
