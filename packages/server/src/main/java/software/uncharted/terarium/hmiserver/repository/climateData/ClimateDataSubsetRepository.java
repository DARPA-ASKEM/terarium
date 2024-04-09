package software.uncharted.terarium.hmiserver.repository.climateData;

import jakarta.validation.constraints.NotNull;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataSubset;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.List;
import java.util.UUID;

public interface ClimateDataSubsetRepository extends PSCrudRepository<ClimateDataSubset, UUID> {
    List<ClimateDataSubset> findByEsgfIdAndEnvelopeAndTimestampsAndThinFactor(@NotNull String esgfId, @NotNull String envelope, String timestamps, String thinFactor);
}
