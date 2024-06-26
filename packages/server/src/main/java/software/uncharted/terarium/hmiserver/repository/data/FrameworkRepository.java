package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface FrameworkRepository extends PSCrudRepository<ModelFramework, UUID> {

	List<ModelFramework> findAllByDeletedOnIsNull();

	List<ModelFramework> findAllByIdInAndDeletedOnIsNull(final List<UUID> ids);

	Optional<ModelFramework> getByIdAndDeletedOnIsNull(final UUID id);
}
