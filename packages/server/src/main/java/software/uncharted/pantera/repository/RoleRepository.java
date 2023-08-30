package software.uncharted.pantera.repository;

import org.springframework.stereotype.Repository;
import software.uncharted.pantera.model.authority.Role;

import java.util.List;
import java.util.Set;

@Repository
public interface RoleRepository extends PSCrudRepository<Role, Long> {
    List<Role> findAllByNameIn(Set<String> roleName);
}
