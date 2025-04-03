package software.uncharted.terarium.hmiserver.repository;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.models.authority.Role;

public interface GroupRepository extends PSCrudRepository<Group, String> {
	Group findByName(String name);
	Collection<Group> findAllByNameIn(Collection<String> names);
	Page<Group> findByNameContainingOrDescriptionContainingAllIgnoreCase(
		final String name,
		final String description,
		final Pageable pageable
	);

	@Query(
		"SELECT r FROM Role r WHERE r NOT IN " +
		"(SELECT x FROM Group g INNER JOIN g.roles x " +
		"WHERE g.id = :groupId) AND (r.name LIKE %:query% OR r.description LIKE %:query%)"
	)
	Page<Role> findUnassignedRoles(@Param("groupId") String groupId, @Param("query") String query, Pageable pageable);
}
