package software.uncharted.terarium.hmiserver.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import software.uncharted.terarium.hmiserver.models.authority.Group;
import software.uncharted.terarium.hmiserver.models.authority.User;

@Repository
@Transactional
public interface UserRepository extends PSCrudRepository<User, String> {
	@Transactional
	Optional<User> findByUsername(String username);

	@Transactional
	Page<User> findByGivenNameContainingOrFamilyNameContainingOrEmailContainingAllIgnoreCase(
		String givenName,
		String familyName,
		String email,
		Pageable pageable
	);

	Stream<User> readAllByIdIn(Collection<String> ids);
	Stream<User> readAllByGroupsContains(Group group);

	@Transactional
	@Query(
		"select u from User u inner join u.groups g where g.id in :ids and (u.givenName like %:givenName% or u.familyName like %:familyName% or u.email like %:email%)"
	)
	Page<User> findGroupMembers(
		@Param("ids") Collection<String> groupIds,
		@Param("givenName") String givenName,
		@Param("familyName") String familyName,
		@Param("email") String email,
		Pageable pageable
	);

	@Transactional
	@Query(
		"select u from User u inner join u.groups g where u.id not in (select x.id from User x inner join x.groups y where y.id in :ids) and (u.givenName like %:givenName% or u.familyName like %:familyName% or u.email like %:email%)"
	)
	Page<User> findGroupNonMembers(
		@Param("ids") Collection<String> groupIds,
		@Param("givenName") String givenName,
		@Param("familyName") String familyName,
		@Param("email") String email,
		Pageable pageable
	);
}
