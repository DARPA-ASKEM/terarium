package software.uncharted.terarium.hmiserver.repository.data;

import org.springframework.stereotype.Repository;

import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface FrameworkRepository extends PSCrudRepository<ModelFramework, String> {

}
