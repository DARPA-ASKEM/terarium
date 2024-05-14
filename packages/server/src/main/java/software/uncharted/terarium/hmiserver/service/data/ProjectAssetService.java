package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.data.ProjectAssetRepository;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacProject;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

	final ProjectAssetRepository projectAssetRepository;

	final ReBACService reBACService;

	/**
	 * Find all active assets for a project. Active assets are defined as those that are not deleted and not temporary.
	 *
	 * @param projectId The project ID
	 * @param types The types of assets to find
	 * @return The list of active assets for the project
	 */
	@Observed(name = "function_profile")
	public List<ProjectAsset> findActiveAssetsForProject(
			@NotNull final UUID projectId,
			final Collection<@NotNull AssetType> types,
			final Schema.Permission hasReadPermission) {
		return projectAssetRepository.findAllByProjectIdAndAssetTypeInAndDeletedOnIsNullAndTemporaryFalse(
				projectId, types);
	}

	@Observed(name = "function_profile")
	public boolean deleteByAssetId(
			@NotNull final UUID projectId,
			@NotNull final AssetType type,
			@NotNull final UUID originalAssetId,
			final Schema.Permission hasWritePermission) {
		final ProjectAsset asset =
				projectAssetRepository.findByProjectIdAndAssetIdAndAssetType(projectId, originalAssetId, type);
		if (asset == null) {
			return false;
		}
		asset.setDeletedOn(Timestamp.from(Instant.now()));
		projectAssetRepository.save(asset);
		return true;
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> createProjectAsset(
			final Project project,
			final AssetType assetType,
			final TerariumAsset asset,
			final Schema.Permission hasWritePermission)
			throws IOException {

		ProjectAsset projectAsset = new ProjectAsset();
		projectAsset.setProject(project);
		projectAsset.setAssetId(asset.getId());
		projectAsset.setAssetType(assetType);
		projectAsset.setAssetName(asset.getName());
		if (asset instanceof Model) {
			projectAsset.setAssetName(((Model) asset).getHeader().getName());
		}

		projectAsset = projectAssetRepository.save(projectAsset);

		project.getProjectAssets().add(projectAsset);

		return Optional.of(projectAsset);
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> updateProjectAsset(
			final ProjectAsset projectAsset, final Schema.Permission hasWritePermission) {
		if (!projectAssetRepository.existsById(projectAsset.getId())) {
			return Optional.empty();
		}
		return Optional.of(projectAssetRepository.save(projectAsset));
	}

	@Observed(name = "function_profile")
	public void updateByAsset(final TerariumAsset asset, final Schema.Permission hasWritePermission) {
		final List<ProjectAsset> projectAssets =
				projectAssetRepository.findByAssetId(asset.getId()).orElse(Collections.emptyList());
		if (!projectAssets.isEmpty()) {
			projectAssets.forEach(projectAsset -> {
				projectAsset.setAssetName(asset.getName());
				updateProjectAsset(projectAsset, hasWritePermission);
			});
		} else {
			log.warn(
					"Could not update the project asset name for asset with id: {} because it does not exist.",
					asset.getId());
		}
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> getProjectAssetByNameAndType(
			final String assetName, final AssetType assetType, final Schema.Permission hasReadPermission) {
		return Optional.ofNullable(
				projectAssetRepository.findByAssetNameAndAssetTypeAndDeletedOnIsNull(assetName, assetType));
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> getProjectAssetByNameAndTypeAndProjectId(
			final UUID projectId,
			final String assetName,
			final AssetType assetType,
			final Schema.Permission hasReadPermission) {
		return Optional.ofNullable(projectAssetRepository.findByProjectIdAndAssetNameAndAssetTypeAndDeletedOnIsNull(
				projectId, assetName, assetType));
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> getProjectAssetByProjectIdAndAssetId(
			final UUID id, final UUID assetId, final Schema.Permission hasReadPermission) {
		return Optional.ofNullable(projectAssetRepository.findByProjectIdAndAssetId(id, assetId));
	}

	@Observed(name = "function_profile")
	public Schema.Permission checkForPermission(
			String userId, final UUID assetId, Schema.Permission desiredPermission) {
		Optional<List<ProjectAsset>> projectAssets = projectAssetRepository.findByAssetId(assetId);
		if (projectAssets.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not associated with a project");
		}
		Schema.Permission foundPermission = null;
		for (ProjectAsset projectAsset : projectAssets.get()) {
			UUID projectId = projectAsset.getProject().getId();
			try {
				final RebacUser rebacUser = new RebacUser(userId, reBACService);
				final RebacProject rebacProject = new RebacProject(projectId, reBACService);
				if (rebacUser.can(rebacProject, desiredPermission)) {
					foundPermission = desiredPermission;
					break;
				}
			} catch (final Exception e) {
				log.error("Error updating project", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update project");
			}
		}
		if (foundPermission != null && foundPermission == desiredPermission) {
			return desiredPermission;
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
	}
}
