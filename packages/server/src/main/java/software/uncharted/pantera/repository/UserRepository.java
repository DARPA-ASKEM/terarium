package software.uncharted.pantera.repository;

import org.springframework.stereotype.Repository;
import software.uncharted.pantera.model.User;

@Repository
public interface UserRepository extends PSCrudRepository<User, String> {
}
