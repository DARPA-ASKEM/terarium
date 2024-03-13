package software.uncharted.terarium.hmiserver.models.climateData;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.UUID;

@Data
public class ClimateDataTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @TSOptional
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    private String climateDataId;
}
