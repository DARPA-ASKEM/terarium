package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

public interface InterventionRepository extends PSCrudSoftDeleteRepository<InterventionPolicy, UUID> {
	List<InterventionPolicy> findByModelIdAndDeletedOnIsNullAndTemporaryFalseOrderByCreatedOnAsc(
		UUID modelId,
		Pageable pageable
	);
}
