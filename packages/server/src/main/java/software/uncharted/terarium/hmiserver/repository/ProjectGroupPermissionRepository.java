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
import software.uncharted.terarium.hmiserver.models.dataservice.project.IProjectGroupPermissionDisplayModel;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectGroupPermission;

@Repository
public interface ProjectGroupPermissionRepository extends PSCrudRepository<ProjectGroupPermission, String> {
	void deleteByProjectIdAndGroupId(UUID projectId, String groupId);
	List<ProjectGroupPermission> findAllByProjectId(UUID projectId);
	ProjectGroupPermission findByProjectIdAndGroupId(UUID projectId, String groupId);
	List<ProjectGroupPermission> findAllByProjectIdAndGroupIdIn(UUID projectId, Collection<String> groupIds);
	void deleteByProjectIdAndGroupIdIn(UUID projectId, Set<String> groupIds);
	void deleteByGroupId(String groupId);
	void deleteByProjectId(UUID projectId);

	@Query(
		"SELECT g as group, g.name as name, p.id as id, p.permissionLevel as permissionLevel FROM Group g " +
		"LEFT JOIN ProjectGroupPermission p ON p.group.id = g.id AND p.project.id = :projectId " +
		"WHERE lower(name) LIKE lower(concat('%', :query, '%')) OR lower(cast(permissionLevel as string)) LIKE lower(concat('%', :query, '%'))"
	)
	Page<IProjectGroupPermissionDisplayModel> findAllByProjectId(
		@Param("projectId") UUID projectId,
		@Param("query") String query,
		Pageable pageable
	);
}
