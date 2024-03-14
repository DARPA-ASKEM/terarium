package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClimateDataResultSubset {
    private String status;
    @JsonProperty("dataset_id")
    private String datasetId;
}
