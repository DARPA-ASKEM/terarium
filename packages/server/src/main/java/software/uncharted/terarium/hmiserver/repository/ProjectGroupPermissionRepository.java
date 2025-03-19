package software.uncharted.terarium.hmiserver.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.project.IProjectGroupPermissionDisplayModel;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectGroupPermission;

@Repository
public interface ProjectGroupPermissionRepository extends PSCrudRepository<ProjectGroupPermission, String> {
	void deleteByProjectIdAndGroupId(String projectId, String groupId);
	List<ProjectGroupPermission> findAllByProjectId(String projectId);
	ProjectGroupPermission findByProjectIdAndGroupId(String projectId, String groupId);
	List<ProjectGroupPermission> findAllByProjectIdAndGroupIdIn(String projectId, Collection<String> groupIds);
	void deleteByProjectIdAndGroupIdIn(String projectId, Set<String> groupIds);
	void deleteByGroupId(String groupId);
	void deleteByProjectId(String projectId);

	@Query(
		"SELECT g as group, g.name as name, p.id as id, p.permissionLevel as permissionLevel FROM Group g " +
		"LEFT JOIN ProjectGroupPermission p ON p.group.id = g.id AND p.project.id = :projectId " +
		"WHERE lower(name) LIKE lower(concat('%', :query, '%')) OR lower(cast(permissionLevel as string)) LIKE lower(concat('%', :query, '%'))"
	)
	Page<IProjectGroupPermissionDisplayModel> findAllByProjectId(
		@Param("projectId") String projectId,
		@Param("query") String query,
		Pageable pageable
	);
}
