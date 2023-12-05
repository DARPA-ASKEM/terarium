package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface FrameworkRepository extends PSCrudRepository<ModelFramework, UUID> {

	List<ModelFramework> findByName(@NotNull String name);

	Long deleteByName(@NotNull String name);

}
