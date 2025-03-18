package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAndAssetAggregate;
import software.uncharted.terarium.hmiserver.repository.UserRepository;
import software.uncharted.terarium.hmiserver.repository.data.ProjectRepository;
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
	final ReBACService reBACService;
	final Messages messages;

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
	public boolean hasPermission(final UUID projectId, final User user, final Schema.Permission permission) {
		try {
			final RebacUser rebacUser = new RebacUser(user.getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(projectId, reBACService);
			return rebacUser.can(rebacProject, permission);
		} catch (final Exception e) {
			log.error("Error checking project permission", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("rebac.service-unavailable"));
		}
	}
}
