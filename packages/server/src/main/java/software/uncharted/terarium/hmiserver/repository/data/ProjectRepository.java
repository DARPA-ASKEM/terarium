package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectRepository extends PSCrudRepository<Project, UUID> {

	List<Project> findAllByIdInAndDeletedOnIsNull(final List<UUID> ids);

	Optional<Project> getByIdAndDeletedOnIsNull(final UUID id);

	@Query(value = "SELECT public_asset FROM project WHERE id = :id", nativeQuery = true)
	Optional<Boolean> findPublicAssetByIdNative(@Param("id") UUID id);
}
