package software.uncharted.terarium.hmiserver.repository.climateData;

import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.climateData.ClimateDataTask;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.UUID;

@Repository
public interface ClimateDataTaskRepository extends PSCrudRepository<ClimateDataTask, UUID> {
}
