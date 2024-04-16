package software.uncharted.terarium.hmiserver.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.DataMigration;

@Repository
public interface DataMigrationRepository extends PSCrudRepository<DataMigration, String> {

	Optional<DataMigration> findByTableName(String tableName);
}
