package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.ChartAnnotation;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

@Repository
public interface ChartAnnotationRepository extends PSCrudSoftDeleteRepository<ChartAnnotation, UUID> {
	List<ChartAnnotation> findByNodeIdAndDeletedOnIsNull(UUID nodeId);
}
