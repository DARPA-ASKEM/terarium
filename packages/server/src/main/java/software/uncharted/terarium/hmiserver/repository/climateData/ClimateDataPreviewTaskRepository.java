package software.uncharted.terarium.hmiserver.repository.climateData;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataPreviewTask;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.UUID;

@Repository
public interface ClimateDataPreviewTaskRepository extends PSCrudRepository<ClimateDataPreviewTask, UUID> {
    ClimateDataPreviewTask findByEsgfIdAndVariableIdAndTimestampsAndTimeIndex(@NotNull String esgfId, @NotNull String variableId, String timestamps, String timeIndex);
}
