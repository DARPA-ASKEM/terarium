package software.uncharted.terarium.hmiserver.repository.data;

import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.data.Project;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.List;

@Repository
public interface ProjectRepository extends PSCrudRepository<Project, String> {

	List<Project> findAll();

	List<Project> findAllById(Iterable<String> ids);
}
