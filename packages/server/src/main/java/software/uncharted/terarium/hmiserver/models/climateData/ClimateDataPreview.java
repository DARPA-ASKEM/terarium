package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.UUID;

@Data
@Entity
public class ClimateDataPreview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @TSOptional
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    private String esgfId;
    private String variableId;
    private String timestamps;
    private String timeIndex;
    private String pngUrl;
    private String error;

    public ClimateDataPreview(ClimateDataPreviewTask previewTask) {
        this.esgfId = previewTask.getEsgfId();
        this.variableId = previewTask.getVariableId();
        this.timestamps = previewTask.getTimestamps();
        this.timeIndex = previewTask.getTimeIndex();
    }

    public ClimateDataPreview(ClimateDataPreviewTask previewTask, JsonNode jobError) {
        this.esgfId = previewTask.getEsgfId();
        this.variableId = previewTask.getVariableId();
        this.timestamps = previewTask.getTimestamps();
        this.timeIndex = previewTask.getTimeIndex();
        this.error = jobError.toString();
    }

    public ClimateDataPreview() {}
}
