package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.authority.Role;
import software.uncharted.terarium.hmiserver.models.authority.RoleType;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.IProjectGroupPermissionDisplayModel;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAndAssetAggregate;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectGroupPermission;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectGroupPermissionDisplayModel;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectPermission;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectPermissionLevel;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectUserPermission;
import software.uncharted.terarium.hmiserver.repository.ProjectGroupPermissionRepository;
import software.uncharted.terarium.hmiserver.repository.ProjectUserPermissionRepository;
import software.uncharted.terarium.hmiserver.repository.UserRepository;
import software.uncharted.terarium.hmiserver.repository.data.ProjectRepository;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.RoleService;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacProject;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectService {

	final ProjectRepository projectRepository;
	final ProjectSearchService projectSearchService;
	final UserRepository userRepository;
	final ProjectGroupPermissionRepository projectGroupPermissionRepository;
	final ProjectUserPermissionRepository projectUserPermissionRepository;
	final CurrentUserService currentUserService;
	final RoleService roleService;
	final ReBACService reBACService;
	final Messages messages;

	private Role ADMIN_ROLE = null;

	@Observed(name = "function_profile")
	public List<Project> getProjects() {
		return projectRepository.findByDeletedOnIsNull();
	}

	@Observed(name = "function_profile")
	public List<Project> getActiveProjects() {
		final Map<UUID, Project> projectMap = new HashMap<>();
		final List<ProjectAndAssetAggregate> projectAggregates = projectRepository.findWithAssets();
		for (final ProjectAndAssetAggregate aggregate : projectAggregates) {
			if (projectMap.containsKey(aggregate.getId())) {
				final Project project = projectMap.get(aggregate.getId());
				addAssetCount(project, aggregate.getAssetType(), aggregate.getAssetCount());
			} else {
				final Project project = new Project();
				project.setId(aggregate.getId());
				project.setCreatedOn(aggregate.getCreatedOn());
				project.setUpdatedOn(aggregate.getUpdatedOn());
				project.setDeletedOn(aggregate.getDeletedOn());
				project.setDescription(aggregate.getDescription());
				project.setFileNames(aggregate.getFileNames());
				project.setPublicAsset(aggregate.getPublicAsset());
				project.setName(aggregate.getName());
				project.setOverviewContent(aggregate.getOverviewContent());
				project.setTemporary(aggregate.getTemporary());
				project.setThumbnail(aggregate.getThumbnail());
				project.setUserId(aggregate.getUserId());
				project.setMetadata(new HashMap<>());
				project.setSampleProject(aggregate.getSampleProject());
				addAssetCount(project, aggregate.getAssetType(), aggregate.getAssetCount());
				projectMap.put(project.getId(), project);
			}
		}
		return new ArrayList<>(projectMap.values());
	}

	@Observed(name = "function_profile")
	public List<Project> getProjects(final List<UUID> ids) {
		return projectRepository.findAllById(ids);
	}

	@Observed(name = "function_profile")
	public List<Project> getActiveProjects(final List<UUID> ids) {
		final Map<UUID, Project> projectMap = new HashMap<>();
		final List<ProjectAndAssetAggregate> projectAggregates = projectRepository.findByIdsWithAssets(ids);
		for (final ProjectAndAssetAggregate aggregate : projectAggregates) {
			if (projectMap.containsKey(aggregate.getId())) {
				final Project project = projectMap.get(aggregate.getId());
				addAssetCount(project, aggregate.getAssetType(), aggregate.getAssetCount());
			} else {
				final Project project = new Project();
				project.setId(aggregate.getId());
				project.setCreatedOn(aggregate.getCreatedOn());
				project.setUpdatedOn(aggregate.getUpdatedOn());
				project.setDeletedOn(aggregate.getDeletedOn());
				project.setDescription(aggregate.getDescription());
				project.setFileNames(aggregate.getFileNames());
				project.setPublicAsset(aggregate.getPublicAsset());
				project.setName(aggregate.getName());
				project.setOverviewContent(aggregate.getOverviewContent());
				project.setTemporary(aggregate.getTemporary());
				project.setThumbnail(aggregate.getThumbnail());
				project.setUserId(aggregate.getUserId());
				project.setMetadata(new HashMap<>());
				project.setSampleProject(aggregate.getSampleProject());
				addAssetCount(project, aggregate.getAssetType(), aggregate.getAssetCount());
				projectMap.put(project.getId(), project);
			}
		}
		return new ArrayList<>(projectMap.values());
	}

	private void addAssetCount(final Project project, final String assetTypeName, final Integer assetCount) {
		if (AssetType.DATASET.name().equals(assetTypeName)) {
			project.getMetadata().put("datasets-count", assetCount.toString());
		}
		if (AssetType.DOCUMENT.name().equals(assetTypeName)) {
			project.getMetadata().put("document-count", assetCount.toString());
		}
		if (AssetType.MODEL.name().equals(assetTypeName)) {
			project.getMetadata().put("models-count", assetCount.toString());
		}
		if (AssetType.WORKFLOW.name().equals(assetTypeName)) {
			project.getMetadata().put("workflows-count", assetCount.toString());
		}
	}

	@Observed(name = "function_profile")
	public Optional<Project> getProject(final UUID id) {
		final Optional<Project> project = projectRepository.getByIdAndDeletedOnIsNull(id);
		if (project.isPresent() && project.get().getUserId() != null) {
			final Optional<User> user = userRepository.findById(project.get().getUserId());
			user.ifPresent(value -> project.get().setUserName(value.getName()));
		}
		return project;
	}

	@Observed(name = "function_profile")
	public Project createProject(final Project project) throws IOException {
		final Project created = projectRepository.save(project);
		projectSearchService.indexProject(project);
		return created;
	}

	@Observed(name = "function_profile")
	public Optional<Project> updateProject(final Project project) throws IOException {
		if (!projectRepository.existsById(project.getId())) {
			return Optional.empty();
		}

		final Project existingProject = projectRepository.getByIdAndDeletedOnIsNull(project.getId()).orElseThrow();

		// merge the existing project with values from the new project
		final Project mergedProject = Project.mergeProjectFields(existingProject, project);

		final Project updated = projectRepository.save(mergedProject);

		projectSearchService.updateProject(updated);

		return Optional.of(updated);
	}

	@Observed(name = "function_profile")
	public boolean delete(final UUID id) throws IOException {
		final Optional<Project> project = getProject(id);
		if (project.isEmpty()) return false;
		project.get().setDeletedOn(Timestamp.from(Instant.now()));
		projectRepository.save(project.get());
		projectSearchService.removeProject(id);
		return true;
	}

	@Observed(name = "function_profile")
	public boolean isProjectPublic(final UUID id) {
		final Optional<Boolean> isPublic = projectRepository.findPublicAssetByIdNative(id);
		return isPublic.orElse(false);
	}

	@Observed(name = "function_profile")
	public boolean hasPermission(final UUID projectId, final User user, final ProjectPermissionLevel level) {
		if (user.hasRole(getAdminRole())) {
			return true;
		}
		final ProjectPermissionLevel permission = getLevel(projectId, user);
		return permission != null && permission.ordinal() >= level.ordinal();
	}

	private Role getAdminRole() {
		if (ADMIN_ROLE == null) {
			ADMIN_ROLE = roleService.getRole(RoleType.ADMIN.name());
		}
		return ADMIN_ROLE;
	}

	/**
	 * Get the maximum permission level for a user for a project
	 *
	 * @param projectId the project
	 * @param user    the user
	 * @return the maximum permission level for the user, or null if the user does not have permission
	 */
	public ProjectPermissionLevel getLevel(final UUID projectId, final User user) {
		final ProjectPermission permission = getPermissions(projectId);
		final ProjectPermissionLevel maxUserPermission = permission
			.getUserPermissions()
			.stream()
			.filter(p -> p.getUser().getId().equals(user.getId()))
			.map(ProjectUserPermission::getPermissionLevel)
			.max(ProjectPermissionLevel::compareTo)
			.orElse(null);

		final Collection<Group> userGroups = user.getGroups();
		final ProjectPermissionLevel maxGroupPermission = userGroups == null
			? null
			: permission
				.getGroupPermissions()
				.stream()
				.filter(p -> userGroups.stream().anyMatch(group -> group.getId().equals(p.getGroup().getId())))
				.map(ProjectGroupPermission::getPermissionLevel)
				.max(ProjectPermissionLevel::compareTo)
				.orElse(null);

		if (maxUserPermission == null && maxGroupPermission == null) {
			return null;
		} else if (maxUserPermission == null) {
			return maxGroupPermission;
		} else if (maxGroupPermission == null) {
			return maxUserPermission;
		} else {
			return maxUserPermission.ordinal() > maxGroupPermission.ordinal() ? maxUserPermission : maxGroupPermission;
		}
	}

	/**
	 * Get the permissions for a project
	 *
	 * @param projectId the project
	 * @return the {@link ProjectPermission} instance
	 */
	public ProjectPermission getPermissions(final UUID projectId) {
		return new ProjectPermission()
			.setProjectId(projectId)
			.setUserPermissions(projectUserPermissionRepository.findAllByProjectId(projectId))
			.setGroupPermissions(projectGroupPermissionRepository.findAllByProjectId(projectId));
	}

	/**
	 * Add a group to a project with a specific permission level
	 *
	 * @param project the project
	 * @param groups  the groups
	 * @param level   the permission level
	 * @return the {@link ProjectGroupPermission} instance
	 */
	@Observed(name = "function_profile")
	public Collection<ProjectGroupPermission> addGroups(
		final Project project,
		final Collection<Group> groups,
		final ProjectPermissionLevel level
	) {
		final User currentUser = currentUserService.get();
		if (currentUser != null && !hasPermission(project.getId(), currentUser, ProjectPermissionLevel.ADMIN)) {
			throw new AccessDeniedException(
				String.format(
					"User %s does not have permission to add groups for project %s",
					currentUser.getId(),
					project.getId()
				)
			);
		}

		final Map<String, ProjectGroupPermission> groupIdToExistingPermissions = projectGroupPermissionRepository
			.findAllByProjectIdAndGroupIdIn(project.getId(), groups.stream().map(Group::getId).collect(Collectors.toList()))
			.stream()
			.collect(Collectors.toMap(p -> p.getGroup().getId(), p -> p));

		final Collection<ProjectGroupPermission> permissions = groups
			.stream()
			.map(group -> {
				final ProjectGroupPermission existingPermission = groupIdToExistingPermissions.get(group.getId());
				if (existingPermission != null) {
					existingPermission.setPermissionLevel(level);
					return existingPermission;
				}
				return new ProjectGroupPermission().setProject(project).setGroup(group).setPermissionLevel(level);
			})
			.collect(Collectors.toList());

		return projectGroupPermissionRepository.saveAll(permissions);
	}

	/**
	 * Add a group to a project with READ permission
	 *
	 * @param project the project
	 * @param group   the group
	 */
	public ProjectGroupPermission addGroup(final Project project, final Group group) {
		return addGroup(project, group, ProjectPermissionLevel.READ);
	}

	/**
	 * Add a group to a project with a specific permission level
	 *
	 * @param project the project
	 * @param group   the group
	 * @param level   the permission level
	 * @return the {@link ProjectGroupPermission} instance
	 */
	public ProjectGroupPermission addGroup(final Project project, final Group group, final ProjectPermissionLevel level) {
		return addGroups(project, Set.of(group), level).stream().findFirst().orElse(null);
	}

	/**
	 * Update a collection of groups' permission level for a project
	 *
	 * @param project the project
	 * @param groups  the groups
	 * @param level   the permission level
	 * @return the {@link ProjectGroupPermission} instances
	 */
	public Collection<ProjectGroupPermission> updateGroups(
		final Project project,
		final Collection<Group> groups,
		final ProjectPermissionLevel level
	) {
		final User currentUser = currentUserService.get();
		if (currentUser != null && !hasPermission(project.getId(), currentUser, ProjectPermissionLevel.ADMIN)) {
			throw new AccessDeniedException(
				String.format(
					"User %s does not have permission to update groups for project %s",
					currentUser.getId(),
					project.getId()
				)
			);
		}

		// Ensure the level being granted is not above the current user's level
		if (currentUser != null) {
			final ProjectUserPermission currentUserPermission = projectUserPermissionRepository.findByProjectIdAndUserId(
				project.getId(),
				currentUser.getId()
			);
			if (currentUserPermission != null && currentUserPermission.getPermissionLevel().ordinal() < level.ordinal()) {
				throw new AccessDeniedException(
					String.format(
						"User %s does not have permission to grant groups %s permission for project %s",
						currentUser.getId(),
						level,
						project.getId()
					)
				);
			}
		}

		final Collection<ProjectGroupPermission> permissions =
			projectGroupPermissionRepository.findAllByProjectIdAndGroupIdIn(
				project.getId(),
				groups.stream().map(Group::getId).collect(Collectors.toSet())
			);
		permissions.forEach(permission -> permission.setPermissionLevel(level));

		return projectGroupPermissionRepository.saveAll(permissions);
	}

	/**
	 * Update a group's permission level for a project
	 *
	 * @param project the project
	 * @param group   the group
	 * @param level   the permission level
	 * @return the {@link ProjectGroupPermission} instance
	 */
	public ProjectGroupPermission updateGroup(
		final Project project,
		final Group group,
		final ProjectPermissionLevel level
	) {
		return updateGroups(project, Set.of(group), level).stream().findFirst().orElse(null);
	}

	/**
	 * Remove a collection of groups from a project
	 *
	 * @param project the project
	 * @param groups  the groups
	 */
	public void removeGroups(final Project project, final Collection<Group> groups) {
		final User currentUser = currentUserService.get();
		if (currentUser != null && !hasPermission(project.getId(), currentUser, ProjectPermissionLevel.ADMIN)) {
			throw new AccessDeniedException(
				String.format(
					"User %s does not have permission to remove groups for project %s",
					currentUser.getId(),
					project.getId()
				)
			);
		}
		projectGroupPermissionRepository.deleteByProjectIdAndGroupIdIn(
			project.getId(),
			groups.stream().map(Group::getId).collect(Collectors.toSet())
		);
		log.info(
			"Removed groups {} from project {}",
			groups.stream().map(Group::getId).collect(Collectors.toList()),
			project.getId()
		);
	}

	/**
	 * Remove a group from a project
	 *
	 * @param project the project
	 * @param group   the group
	 */
	public void removeGroup(final Project project, final Group group) {
		removeGroups(project, Set.of(group));
	}

	/**
	 * Gets a page of groups and their associated permissions for the given project/filter/pageable instance.
	 * This function uses the {@link IProjectGroupPermissionDisplayModel} interface to fetch the joined data from the Group and ProjectGroupPermission tables.
	 * We then create a {@link ProjectGroupPermissionDisplayModel} instance for each group, and set the inherited permission level for each group as well
	 * as provide serialization methods for the case when the group does not have a permission level.
	 *
	 * @param project  the project
	 * @param query    the optional query to filter the groups
	 * @param pageable the pageable instance
	 * @return a page of groups and their associated permissions
	 */
	public Page<ProjectGroupPermissionDisplayModel> getGroups(
		final Project project,
		final String query,
		final Pageable pageable
	) {
		final Page<IProjectGroupPermissionDisplayModel> page = projectGroupPermissionRepository.findAllByProjectId(
			project.getId(),
			query,
			pageable
		);
		final List<ProjectGroupPermissionDisplayModel> content = page
			.getContent()
			.stream()
			.map(ProjectGroupPermissionDisplayModel::new)
			.toList();
		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	/**
	 * Add a user to a project with a specific permission level
	 *
	 * @param project the project
	 * @param users   the users
	 * @param level   the permission level
	 * @return the {@link ProjectUserPermission} instance
	 */
	@Observed(name = "function_profile")
	public Collection<ProjectUserPermission> addUsers(
		final Project project,
		final Collection<User> users,
		final ProjectPermissionLevel level
	) {
		final User currentUser = currentUserService.get();
		if (currentUser != null && !hasPermission(project.getId(), currentUser, ProjectPermissionLevel.ADMIN)) {
			throw new AccessDeniedException(
				String.format(
					"User %s does not have permission to add users for project %s",
					currentUser.getId(),
					project.getId()
				)
			);
		}

		final Map<String, ProjectUserPermission> userIdToExistingPermissions = projectUserPermissionRepository
			.findAllByProjectIdAndUserIdIn(project.getId(), users.stream().map(User::getId).collect(Collectors.toList()))
			.stream()
			.collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

		final Collection<ProjectUserPermission> permissions = users
			.stream()
			.map(user -> {
				final ProjectUserPermission existingPermission = userIdToExistingPermissions.get(user.getId());
				if (existingPermission != null) {
					existingPermission.setPermissionLevel(level);
					return existingPermission;
				}
				return new ProjectUserPermission().setProject(project).setUser(user).setPermissionLevel(level);
			})
			.collect(Collectors.toList());

		return projectUserPermissionRepository.saveAll(permissions);
	}

	/**
	 * Add a user to a project with READ permission
	 *
	 * @param project the project
	 * @param user    the user
	 * @return the {@link ProjectUserPermission} instance
	 */
	public ProjectUserPermission addUser(final Project project, final User user) {
		return addUsers(project, Set.of(user), ProjectPermissionLevel.READ).stream().findFirst().orElse(null);
	}

	/**
	 * Add a user to a project with a specific permission level
	 *
	 * @param project the project
	 * @param user    the user
	 * @param level   the permission level
	 * @return the {@link ProjectUserPermission} instance
	 */
	public ProjectUserPermission addUser(final Project project, final User user, final ProjectPermissionLevel level) {
		return addUsers(project, Set.of(user), level).stream().findFirst().orElse(null);
	}

	/**
	 * Remove a collection of users from a project
	 *
	 * @param project the project
	 * @param users   the users
	 */
	public void removeUsers(final Project project, final Collection<User> users) {
		final User currentUser = currentUserService.get();
		if (currentUser != null && !hasPermission(project.getId(), currentUser, ProjectPermissionLevel.ADMIN)) {
			throw new AccessDeniedException(
				String.format(
					"User %s does not have permission to remove users for project %s",
					currentUser.getId(),
					project.getId()
				)
			);
		}
		projectUserPermissionRepository.deleteByProjectIdAndUserIdIn(
			project.getId(),
			users.stream().map(User::getId).collect(Collectors.toSet())
		);

		log.info(
			"Removed users {} from project {}",
			users.stream().map(User::getId).collect(Collectors.toList()),
			project.getId()
		);
	}

	/**
	 * Remove a user from a project
	 *
	 * @param project the project
	 * @param user    the user
	 */
	public void removeUser(final Project project, final User user) {
		removeUsers(project, Set.of(user));
	}

	/**
	 * This is a temporary method to convert the project permission level to a rebac permission.
	 * This will be removed once the rebac service is fully removed
	 * @param level the project permission level
	 * @return the rebac permission
	 */
	public static Schema.Permission projectPermissionToRebacPermission(ProjectPermissionLevel level) {
		switch (level) {
			case ADMIN:
				return Schema.Permission.ADMINISTRATE;
			case OWNER, WRITE:
				return Schema.Permission.WRITE;
			case READ:
				return Schema.Permission.READ;
			default:
				return Schema.Permission.NONE;
		}
	}
}
