package software.uncharted.terarium.hmiserver.repository.data;

import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.data.project.Project;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;


import java.util.UUID;

@Repository
public interface ProjectRepository extends PSCrudRepository<Project, UUID> {


}
