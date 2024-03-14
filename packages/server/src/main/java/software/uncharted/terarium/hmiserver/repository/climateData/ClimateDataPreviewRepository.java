package software.uncharted.terarium.hmiserver.repository.climateData;

import jakarta.validation.constraints.NotNull;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataPreview;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.UUID;

public interface ClimateDataPreviewRepository extends PSCrudRepository<ClimateDataPreview, UUID> {
    ClimateDataPreview findByEsfgIdAndVariableIdAndTimestampsAndTimeIndex(@NotNull String esfgId, @NotNull String variableId, String timestamps, String timeIndex);
}
