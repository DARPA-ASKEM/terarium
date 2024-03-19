package software.uncharted.terarium.hmiserver.repository.climateData;

import jakarta.validation.constraints.NotNull;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataSubsetTask;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.UUID;

public interface ClimateDataSubsetTaskRepository extends PSCrudRepository<ClimateDataSubsetTask, UUID> {
    ClimateDataSubsetTask findByEsgfIdAndEnvelopeAndTimestampsAndThinFactor(@NotNull String esgfId, @NotNull String envelope, String timestamps, String thinFactor);
}
