package software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class CiemssCalibrateStatusUpdate extends CiemssStatusUpdate {

	private Number loss;
}
