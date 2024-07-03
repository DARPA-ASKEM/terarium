package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectRepository extends PSCrudRepository<Project, UUID> {

	List<Project> findAllByIdInAndDeletedOnIsNull(final List<UUID> ids);

	Optional<Project> getByIdAndDeletedOnIsNull(final UUID id);

	@Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.id = :id AND p.publicAsset = true")
	boolean isPublicAssetById(final UUID id);

}
