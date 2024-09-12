package software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates;

import com.fasterxml.jackson.annotation.JsonValue;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
public enum CiemssStatusType {
	OPTIMIZE("optimize"),
	CALIBRATE("calibrate");

	public final String type;

	CiemssStatusType(final String type) {
		this.type = type;
	}

	@Override
	@JsonValue
	public String toString() {
		return type;
	}
}
