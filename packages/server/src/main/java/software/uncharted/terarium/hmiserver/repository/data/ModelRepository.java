package software.uncharted.terarium.hmiserver.repository.data;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

@Repository
public interface ModelRepository extends PSCrudSoftDeleteRepository<Model, UUID> {
	@Query(
		value = "SELECT m.* FROM Model m " +
		"INNER JOIN model_configuration mc ON m.id = mc.model_id " +
		"WHERE mc.id = :modelConfigurationId",
		nativeQuery = true
	)
	Optional<Model> findModelByModelConfigurationId(@Param("modelConfigurationId") UUID modelConfigurationId);
}
