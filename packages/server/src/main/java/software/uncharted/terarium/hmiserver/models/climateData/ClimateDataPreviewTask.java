package software.uncharted.terarium.hmiserver.models.climateData;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Entity
public class ClimateDataPreviewTask {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String statusId;
	private String esgfId;
	private String variableId;
	private String timestamps;
	private String timeIndex;

	public ClimateDataPreviewTask(
		String statusId,
		String esgfId,
		String variableId,
		String timestamps,
		String timeIndex
	) {
		this.statusId = statusId;
		this.esgfId = esgfId;
		this.variableId = variableId;
		this.timestamps = timestamps;
		this.timeIndex = timeIndex;
	}

	public ClimateDataPreviewTask() {}
}
