package software.uncharted.terarium.hmiserver.repository;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.authority.Role;

@Repository
public interface RoleRepository extends PSCrudRepository<Role, Long> {
	List<Role> findAllByNameIn(Set<String> roleName);
	Role findByName(String name);
}
