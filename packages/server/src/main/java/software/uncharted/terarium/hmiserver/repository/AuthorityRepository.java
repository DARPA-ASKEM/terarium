package software.uncharted.terarium.hmiserver.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.authority.Authority;

@Repository
public interface AuthorityRepository extends PSCrudRepository<Authority, Long> {
    Optional<Authority> findFirstByName(String name);
}
