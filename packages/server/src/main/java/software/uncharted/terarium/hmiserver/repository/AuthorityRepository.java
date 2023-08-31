package software.uncharted.terarium.hmiserver.repository;

import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.model.authority.Authority;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends PSCrudRepository<Authority, Long> {
    Optional<Authority> findFirstByName(String name);
}
