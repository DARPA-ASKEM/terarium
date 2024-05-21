package software.uncharted.terarium.hmiserver.repository.data;

import java.util.UUID;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

public interface DatasetRepository extends PSCrudSoftDeleteRepository<Dataset, UUID> {}
