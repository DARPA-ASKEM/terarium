package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class ClimateDataResultSubset {
    private String status;
    @JsonProperty("dataset_id")
    private UUID datasetId;
}
