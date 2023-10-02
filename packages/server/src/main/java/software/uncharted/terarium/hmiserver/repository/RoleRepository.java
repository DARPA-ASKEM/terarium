package software.uncharted.terarium.hmiserver.repository;

import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.authority.Role;

import java.util.List;
import java.util.Set;

@Repository
public interface RoleRepository extends PSCrudRepository<Role, Long> {
	List<Role> findAllByNameIn(Set<String> roleName);
}
