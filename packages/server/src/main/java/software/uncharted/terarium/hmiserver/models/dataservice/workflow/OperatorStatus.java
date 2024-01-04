package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

public enum OperatorStatus {
	/*
	export enum OperatorStatus {
	DEFAULT = 'default',
	IN_PROGRESS = 'in progress',
	SUCCESS = 'success',
	INVALID = 'invalid',
	WARNING = 'warning', // Probably won't be used - would there be potential crossover with INVALID?
	FAILED = 'failed',
	ERROR = 'error',
	DISABLED = 'disabled'
}
	 */
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
