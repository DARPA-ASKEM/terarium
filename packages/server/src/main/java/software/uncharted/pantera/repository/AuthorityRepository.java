package software.uncharted.pantera.repository;

import org.springframework.stereotype.Repository;
import software.uncharted.pantera.model.authority.Authority;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends PSCrudRepository<Authority, Long> {
    Optional<Authority> findFirstByName(String name);
}
