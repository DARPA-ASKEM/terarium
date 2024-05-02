package software.uncharted.terarium.hmiserver.repository.data;

import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;
import java.util.UUID;
public interface DatasetRepository extends PSCrudSoftDeleteRepository<Dataset, UUID> {}

