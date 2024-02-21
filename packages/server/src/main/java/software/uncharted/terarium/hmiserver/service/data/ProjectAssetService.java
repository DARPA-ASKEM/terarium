package software.uncharted.terarium.hmiserver.service.data;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.data.ProjectAssetRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

	final ProjectAssetRepository projectAssetRepository;

	public List<ProjectAsset> findAllByProjectId(@NotNull final UUID projectId) {
		return projectAssetRepository.findAllByProjectId(projectId);
	}

	/**
	 * Find all active assets for a project.  Active assets are defined as those that are not deleted and not temporary.
	 * @param projectId The ID of the project to find assets for
	 * @param types The types of assets to find
	 * @return A list of active assets for the project
	 */
	public List<ProjectAsset> findActiveAssetsForProject(@NotNull final UUID projectId,
			final Collection<@NotNull AssetType> types) {
		return projectAssetRepository.findAllByProjectIdAndAssetTypeInAndDeletedOnIsNullAndTemporaryFalse(projectId, types);
	}

	public ProjectAsset save(final ProjectAsset asset) {
		return projectAssetRepository.save(asset);
	}

	public boolean deleteByAssetId(@NotNull final UUID projectId, @NotNull final AssetType type,
			@NotNull final UUID originalAssetId) {
		final ProjectAsset asset = projectAssetRepository
				.findByProjectIdAndAssetIdAndAssetType(projectId, originalAssetId, type);
		if (asset == null) {
			return false;
		}
		asset.setDeletedOn(Timestamp.from(Instant.now()));
		return (save(asset) != null);
	}

	public boolean delete(final UUID id) {
		final ProjectAsset asset = projectAssetRepository.findById(id).orElse(null);
		if (asset == null) {
			return false;
		}
		asset.setDeletedOn(Timestamp.from(Instant.now()));
		return (save(asset) != null);
	}

	public ProjectAsset findByProjectIdAndAssetIdAndAssetType(@NotNull final UUID projectId,
			@NotNull final UUID assetId,
			@NotNull final AssetType type) {
		return projectAssetRepository.findByProjectIdAndAssetIdAndAssetType(projectId, assetId, type);
	}

	public Optional<ProjectAsset> createProjectAsset(final Project project, final AssetType assetType, final TerariumAsset asset)
			throws IOException {

		final ProjectAsset projectAsset = new ProjectAsset();
		projectAsset.setProject(project);
		projectAsset.setAssetId(asset.getId());
		projectAsset.setAssetType(assetType);
		projectAsset.setAssetName(asset.getName());

		if (project.getProjectAssets() == null) {
			project.setProjectAssets(new ArrayList<>(List.of(projectAsset)));
		} else {
			project.getProjectAssets().add(projectAsset);
		}

		return Optional.of(projectAssetRepository.save(projectAsset));
	}

	public Optional<ProjectAsset> updateProjectAsset(final ProjectAsset projectAsset) {
		if (!projectAssetRepository.existsById(projectAsset.getId())) {
			return Optional.empty();
		}
		return Optional.of(projectAssetRepository.save(projectAsset));
	}

	public void updateByAsset(final TerariumAsset asset) {
		final Optional<ProjectAsset> projectAsset = projectAssetRepository.findByAssetId(asset.getId());
		if (projectAsset.isPresent()) {
			projectAsset.get().setAssetName(asset.getName());
			updateProjectAsset(projectAsset.get());
		} else {
			log.info("Could not update the project asset name for asset with id: " + asset.getId() + " because it does not exist.");
		}
	}

	public Optional<ProjectAsset> getProjectAssetByNameAndType(final String assetName, final AssetType assetType) {
		return Optional
				.ofNullable(projectAssetRepository.findByAssetNameAndAssetTypeAndDeletedOnIsNull(assetName, assetType));
	}

	public Optional<ProjectAsset> getProjectAssetByNameAndTypeAndProjectId(final UUID projectId, final String assetName,
			final AssetType assetType) {
		return Optional.ofNullable(projectAssetRepository
				.findByProjectIdAndAssetNameAndAssetTypeAndDeletedOnIsNull(projectId, assetName, assetType));
	}

}
