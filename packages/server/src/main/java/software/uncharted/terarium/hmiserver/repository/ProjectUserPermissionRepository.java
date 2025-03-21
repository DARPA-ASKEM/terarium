package software.uncharted.terarium.hmiserver.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.project.IProjectUserPermissionDisplayModel;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectUserPermission;

@Repository
public interface ProjectUserPermissionRepository extends PSCrudRepository<ProjectUserPermission, String> {
	void deleteByProjectIdAndUserId(UUID projectId, String userId);
	List<ProjectUserPermission> findAllByProjectId(UUID projectId);
	ProjectUserPermission findByProjectIdAndUserId(UUID projectId, String userId);
	List<ProjectUserPermission> findAllByProjectIdAndUserIdIn(UUID projectId, Collection<String> userIds);
	void deleteByProjectIdAndUserIdIn(UUID projectId, Set<String> userIds);
	void deleteByProjectId(UUID projectId);

	@Query(
		"SELECT u as user, u.email as email, u.familyName as familyName, u.givenName as givenName, u.username as username, p.id as id, p.permissionLevel as permissionLevel FROM User u " +
		"LEFT JOIN ProjectUserPermission p ON p.user.id = u.id AND p.project.id = :projectId " +
		"WHERE lower(email) LIKE lower(concat('%', :query, '%')) OR lower(familyName) LIKE lower(concat('%', :query, '%')) OR lower(givenName) LIKE lower(concat('%', :query, '%')) OR lower(username) LIKE lower(concat('%', :query, '%')) OR lower(cast(permissionLevel as string)) LIKE lower(concat('%', :query, '%'))"
	)
	Page<IProjectUserPermissionDisplayModel> findAllByProjectId(
		@Param("projectId") UUID projectId,
		@Param("query") String query,
		Pageable pageable
	);
}
