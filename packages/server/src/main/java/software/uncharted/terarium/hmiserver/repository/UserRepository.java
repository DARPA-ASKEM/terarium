package software.uncharted.terarium.hmiserver.repository;

import java.util.Collection;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.models.User;

@Repository
public interface UserRepository extends PSCrudRepository<User, String> {
	Stream<User> readAllByIdIn(Collection<String> ids);
	Stream<User> readAllByGroupsContains(Group group);
}
