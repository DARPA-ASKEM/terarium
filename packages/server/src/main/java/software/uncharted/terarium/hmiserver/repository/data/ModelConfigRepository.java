package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

@Repository
public interface ModelConfigRepository extends PSCrudSoftDeleteRepository<ModelConfiguration, UUID> {
	List<ModelConfiguration> findByModelIdAndDeletedOnIsNullAndTemporaryFalseOrderByCreatedOnAsc(
		UUID modelId,
		Pageable pageable
	);
}
