package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectRepository extends PSCrudRepository<Project, UUID> {

	List<Project> findAllByIdAndDeletedOnIsNull(List<UUID> ids);
}
