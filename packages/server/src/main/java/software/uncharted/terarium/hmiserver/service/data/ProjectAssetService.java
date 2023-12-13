package software.uncharted.terarium.hmiserver.service.data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.data.project.Project;
import software.uncharted.terarium.hmiserver.models.data.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.data.project.ResourceType;
import software.uncharted.terarium.hmiserver.repository.data.ProjectAssetRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

	final ProjectAssetRepository projectAssetRepository;

	public List<ProjectAsset> findAllByProjectId(@NotNull UUID projectId) {
		return projectAssetRepository.findAllByProjectId(projectId);
	}

	public List<ProjectAsset> findActiveAssetsForProject(@NotNull UUID projectId,
			Collection<@NotNull ResourceType> types) {
		return projectAssetRepository.findAllByProjectIdAndResourceTypeInAndDeletedOnIsNull(projectId, types);
	}

	public ProjectAsset save(final ProjectAsset asset) {
		return projectAssetRepository.save(asset);
	}

	public ProjectAsset findByProjectIdAndResourceIdAndResourceType(@NotNull UUID projectId, @NotNull String resourceId,
			@NotNull ResourceType type) {
		return projectAssetRepository.findByProjectIdAndResourceIdAndResourceType(projectId, resourceId, type);
	}

	public ProjectAsset createProjectAsset(final Project project, final ResourceType type, final UUID resourceId) {

		ProjectAsset asset = new ProjectAsset();
		project.getProjectAssets().add(asset);
		asset.setProject(project);
		asset.setResourceType(type);
		asset.setResourceId(resourceId);

		return projectAssetRepository.save(asset);
	}

}
