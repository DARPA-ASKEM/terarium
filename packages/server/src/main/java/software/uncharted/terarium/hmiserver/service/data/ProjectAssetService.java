package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
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
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

	final ProjectAssetRepository projectAssetRepository;

	final ReBACService reBACService;

	final ProjectService projectService;

	final Messages messages;

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
		final Schema.Permission hasReadPermission
	) {
		return projectAssetRepository.findAllByProjectIdAndAssetTypeInAndDeletedOnIsNullAndTemporaryFalse(projectId, types);
	}

	@Observed(name = "function_profile")
	public boolean deleteByAssetId(
		@NotNull final UUID projectId,
		@NotNull final AssetType type,
		@NotNull final UUID originalAssetId,
		final Schema.Permission hasWritePermission
	) {
		final ProjectAsset asset = projectAssetRepository.findByProjectIdAndAssetIdAndAssetType(
			projectId,
			originalAssetId,
			type
		);
		if (asset == null) {
			return false;
		}
		asset.setDeletedOn(Timestamp.from(Instant.now()));
		projectAssetRepository.save(asset);
		return true;
	}

	@Observed(name = "function_profile")
	public ProjectAsset createProjectAsset(
		final UUID projectId,
		final AssetType assetType,
		final TerariumAsset asset,
		final Schema.Permission hasWritePermission
	) {
		final Optional<Project> project;
		try {
			project = projectService.getProject(projectId);
			if (!project.isPresent()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("projects.not-found"));
			}
		} catch (final Exception e) {
			log.error("Error communicating with project service", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		ProjectAsset projectAsset = new ProjectAsset();
		projectAsset.setProject(project.get());
		projectAsset.setAssetId(asset.getId());
		projectAsset.setAssetType(assetType);
		projectAsset.setAssetName(asset.getName());

		projectAsset = projectAssetRepository.save(projectAsset);

		project.get().getProjectAssets().add(projectAsset);

		return projectAsset;
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> updateProjectAsset(
		final ProjectAsset projectAsset,
		final Schema.Permission hasWritePermission
	) {
		if (!projectAssetRepository.existsById(projectAsset.getId())) {
			return Optional.empty();
		}
		return Optional.of(projectAssetRepository.save(projectAsset));
	}

	@Observed(name = "function_profile")
	public void updateByAsset(final TerariumAsset asset, final Schema.Permission hasWritePermission) {
		final List<ProjectAsset> projectAssets = projectAssetRepository.findByAssetId(asset.getId());
		if (!projectAssets.isEmpty()) {
			projectAssets.forEach(projectAsset -> {
				projectAsset.setAssetName(asset.getName());
				updateProjectAsset(projectAsset, hasWritePermission);
			});
		} else {
			log.warn(
				"Could not update the project asset name for asset with id: {} because it does not exist.",
				asset.getId()
			);
		}
	}

	@Observed(name = "function_profile")
	public boolean isPartOfExistingProject(final UUID assetId) {
		final List<ProjectAsset> projects = projectAssetRepository.findByAssetId(assetId);
		return !projects.isEmpty();
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> getProjectAssetByNameAndType(
		final String assetName,
		final AssetType assetType,
		final Schema.Permission hasReadPermission
	) {
		return Optional.ofNullable(
			projectAssetRepository.findByAssetNameAndAssetTypeAndDeletedOnIsNull(assetName, assetType)
		);
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> getProjectAssetByNameAndTypeAndProjectId(
		final UUID projectId,
		final String assetName,
		final AssetType assetType,
		final Schema.Permission hasReadPermission
	) {
		return Optional.ofNullable(
			projectAssetRepository.findByProjectIdAndAssetNameAndAssetTypeAndDeletedOnIsNull(projectId, assetName, assetType)
		);
	}

	@Observed(name = "function_profile")
	public Optional<ProjectAsset> getProjectAssetByProjectIdAndAssetId(
		final UUID id,
		final UUID assetId,
		final Schema.Permission hasReadPermission
	) {
		return Optional.ofNullable(projectAssetRepository.findByProjectIdAndAssetId(id, assetId));
	}

	@Observed(name = "function_profile")
	public List<ProjectAsset> getProjectAssets(final UUID projectId, final Schema.Permission hasReadPermission) {
		return projectAssetRepository.findAllByProjectIdAndDeletedOnIsNullAndTemporaryFalse(projectId);
	}

	@Observed(name = "function_profile")
	public UUID getProjectIdForAsset(final UUID assetId, final Schema.Permission hasReadPermission) {
		final List<ProjectAsset> assets = projectAssetRepository.findByAssetId(assetId);

		for (final ProjectAsset asset : assets) {
			if (asset.getProject() != null) {
				return asset.getProject().getId();
			}
		}
		return null;
	}

	@Observed(name = "function_profile")
	public void togglePublicForAssets(
		final TerariumAssetServices terariumAssetServices,
		final UUID projectId,
		final boolean isPublic,
		final Schema.Permission hasWritePermission
	) throws IOException {
		final List<ProjectAsset> projectAssets =
			projectAssetRepository.findAllByProjectIdAndDeletedOnIsNullAndTemporaryFalse(projectId);

		for (final ProjectAsset projectAsset : projectAssets) {
			final ITerariumAssetService<? extends TerariumAsset> terariumAssetService =
				terariumAssetServices.getServiceByType(projectAsset.getAssetType());

			final Optional<? extends TerariumAsset> asset = terariumAssetService.getAsset(
				projectAsset.getAssetId(),
				hasWritePermission
			);

			if (asset.isPresent()) {
				asset.get().setPublicAsset(isPublic);

				terariumAssetServices.updateAsset(
					(TerariumAsset) asset.get(),
					projectId,
					projectAsset.getAssetType(),
					hasWritePermission
				);
			}
		}
	}
}
