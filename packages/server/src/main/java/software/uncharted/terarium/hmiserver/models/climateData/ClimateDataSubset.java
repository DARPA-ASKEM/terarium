package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Entity
public class ClimateDataSubset {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String esgfId;
	private String envelope;
	private String timestamps;
	private String thinFactor;
	private UUID datasetId;

	@Column(length = 4096)
	private String error;

	public ClimateDataSubset(ClimateDataSubsetTask subsetTask) {
		this.esgfId = subsetTask.getEsgfId();
		this.envelope = subsetTask.getEnvelope();
		this.timestamps = subsetTask.getTimestamps();
		this.thinFactor = subsetTask.getThinFactor();
	}

	public ClimateDataSubset(ClimateDataSubsetTask subsetTask, String error) {
		this(subsetTask);
		this.error = error;
	}

	public ClimateDataSubset(ClimateDataSubsetTask subsetTask, JsonNode jobError) {
		this(subsetTask, jobError.toString());
	}

	public ClimateDataSubset() {}

	public ClimateDataSubset(ClimateDataSubsetTask subsetTask, UUID datasetId) {
		this(subsetTask);
		this.datasetId = datasetId;
	}
}
