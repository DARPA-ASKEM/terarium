package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

public enum OperatorStatus {

	DEFAULT("default"),
	IN_PROGRESS("in progress"),
	SUCCESS("success"),
	INVALID("invalid"),
	WARNING("warning"),
	FAILED("failed"),
	ERROR("error"),
	DISABLED("disabled");

	private String value;

	OperatorStatus(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
