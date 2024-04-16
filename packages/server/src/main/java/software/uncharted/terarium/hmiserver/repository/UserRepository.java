package software.uncharted.terarium.hmiserver.repository;

import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.User;

@Repository
public interface UserRepository extends PSCrudRepository<User, String> {}
