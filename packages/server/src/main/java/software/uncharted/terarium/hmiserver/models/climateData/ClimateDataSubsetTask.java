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
public class ClimateDataSubsetTask {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String statusId;
	private String esgfId;
	private String envelope;
	private String timestamps;
	private String thinFactor;

	public ClimateDataSubsetTask(String statusId, String esgfId, String envelope, String timestamps, String thinFactor) {
		this.statusId = statusId;
		this.esgfId = esgfId;
		this.envelope = envelope;
		this.timestamps = timestamps;
		this.thinFactor = thinFactor;
	}

	public ClimateDataSubsetTask() {}
}
