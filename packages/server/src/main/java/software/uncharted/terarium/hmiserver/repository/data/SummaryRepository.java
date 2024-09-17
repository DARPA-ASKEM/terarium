package software.uncharted.terarium.hmiserver.repository.data;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.Summary;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

@Repository
public interface SummaryRepository extends PSCrudSoftDeleteRepository<Summary, UUID> {}
