package software.uncharted.terarium.hmiserver.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.authority.Role;

@Repository
public interface RoleRepository extends PSCrudRepository<Role, Long> {
	Page<Role> findByNameContainingOrDescriptionContainingAllIgnoreCase(
		String name,
		String description,
		Pageable pageable
	);
	List<Role> findAllByNameIn(Set<String> roleName);
	List<Role> findAllByIdNotIn(Set<Long> roleIds);
	Role findByName(String name);
}
