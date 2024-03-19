package software.uncharted.terarium.hmiserver.models.extractionservice;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.UUID;

@Data
@TSModel
public class ExtrationStatusUpdate {
    private UUID documentId;
    private Integer step;
    private Integer totalSteps;
    private String message;
    private String error;

    public ExtrationStatusUpdate(UUID documentId, Integer step, Integer totalSteps, String message, String error) {
        this.documentId = documentId;
        this.step = step;
        this.totalSteps = totalSteps;
        this.message = message;
        this.error = error;
    }
}
