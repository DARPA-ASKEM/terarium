package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;

@Data
public class ClimateDataResultSubset {

	private String status;

	@JsonProperty("dataset_id")
	private UUID datasetId;

	private String error;
}
