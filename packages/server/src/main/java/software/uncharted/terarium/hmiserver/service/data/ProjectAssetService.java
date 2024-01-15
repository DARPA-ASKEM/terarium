package software.uncharted.terarium.hmiserver.service.data;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.data.ProjectAssetRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

	final ProjectAssetRepository projectAssetRepository;

	public List<ProjectAsset> findAllByProjectId(@NotNull final UUID projectId) {
		return projectAssetRepository.findAllByProjectId(projectId);
	}

	public List<ProjectAsset> findActiveAssetsForProject(@NotNull final UUID projectId,
                                                         final Collection<@NotNull AssetType> types) {
		return projectAssetRepository.findAllByProjectIdAndAssetTypeInAndDeletedOnIsNull(projectId, types);
	}

	public ProjectAsset save(final ProjectAsset asset) {
		return projectAssetRepository.save(asset);
	}

	public boolean delete(final UUID id) {
		final ProjectAsset asset = projectAssetRepository.findById(id).orElse(null);
		if (asset == null)
			return false;
		asset.setDeletedOn(Timestamp.from(Instant.now()));
		return (save(asset) != null);
	}

	public ProjectAsset findByProjectIdAndAssetIdAndAssetType(@NotNull final UUID projectId, @NotNull final UUID assetId,
                                                                 @NotNull final AssetType type) {
		return projectAssetRepository.findByProjectIdAndAssetIdAndAssetType(projectId, assetId, type);
	}

	public ProjectAsset createProjectAsset(final Project project, final AssetType type, final UUID assetId) {

		final ProjectAsset asset = new ProjectAsset();
		project.getProjectAssets().add(asset);
		asset.setProject(project);
		asset.setAssetType(type);
		asset.setAssetId(assetId);
		asset.setCreatedOn(Timestamp.from(Instant.now()));

		return projectAssetRepository.save(asset);
	}

	public Optional<ProjectAsset> updateProjectAsset(final ProjectAsset asset) {
		if (!projectAssetRepository.existsById(asset.getId())) {
			return Optional.empty();
		}
		return Optional.of(projectAssetRepository.save(asset));
	}

}
