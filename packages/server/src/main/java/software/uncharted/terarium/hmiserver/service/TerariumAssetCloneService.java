package software.uncharted.terarium.hmiserver.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetExport;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.FileExport;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectExport;
import software.uncharted.terarium.hmiserver.service.data.ITerariumAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.TerariumAssetServices;
import software.uncharted.terarium.hmiserver.utils.AssetDependencyUtil;
import software.uncharted.terarium.hmiserver.utils.AssetDependencyUtil.AssetDependencyMap;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@RequiredArgsConstructor
public class TerariumAssetCloneService {

	final private ProjectService projectService;
	final private ProjectAssetService projectAssetService;
	final private TerariumAssetServices terariumAssetServices;

	public void cloneAndPersistAsset(final UUID projectId, final UUID assetId) throws IOException {

		final List<ProjectAsset> projectAssets = projectAssetService.getProjectAssets(projectId,
				Schema.Permission.READ);
		final Map<UUID, ProjectAsset> projectAssetsById = new HashMap<>();
		final Set<UUID> projectAssetIds = new HashSet<>();

		for (final ProjectAsset projectAsset : projectAssets) {
			projectAssetsById.put(projectAsset.getAssetId(), projectAsset);
			projectAssetIds.add(projectAsset.getAssetId());
		}

		final Stack<UUID> assetsToClone = new Stack<>();
		assetsToClone.push(assetId);

		final Map<UUID, AssetDependencyMap> assetDependencies = new HashMap<>();

		final Map<UUID, TerariumAsset> clonedAssets = new HashMap<>();

		final Map<UUID, UUID> oldToNewIds = new HashMap<>();

		while (assetsToClone.size() > 0) {

			final UUID currentAssetId = assetsToClone.pop();

			if (oldToNewIds.containsKey(currentAssetId)) {
				// already cloned this asset
				continue;
			}

			final ProjectAsset currentProjectAsset = projectAssetsById.get(currentAssetId);

			final ITerariumAssetService<? extends TerariumAsset> terariumAssetService = terariumAssetServices
					.getServiceByType(currentProjectAsset.getAssetType());

			final TerariumAsset currentAsset = terariumAssetService.getAsset(currentAssetId, Schema.Permission.READ)
					.orElseThrow();

			final AssetDependencyMap dependencies = AssetDependencyUtil.getAssetDependencies(projectAssetIds,
					currentAsset);

			for (final UUID dependencyId : dependencies.getIds()) {
				assetsToClone.push(dependencyId);
			}

			assetDependencies.put(currentAssetId, dependencies);

			// clone the asset
			final TerariumAsset clonedAsset = currentAsset.clone();
			clonedAssets.put(clonedAsset.getId(), clonedAsset);

			// store the id mapping
			oldToNewIds.put(currentAssetId, clonedAsset.getId());

			// copy the files to new buckets
			terariumAssetService.copyAssetFiles(clonedAsset, currentAsset, Schema.Permission.WRITE);
		}

		// update all uuids with the cloned uuids

		for (final TerariumAsset clonedAsset : clonedAssets.values()) {

			final AssetDependencyMap dependencies = assetDependencies.get(clonedAsset.getId());

			// update any referenced dependencies
			final TerariumAsset finalClonedAsset = AssetDependencyUtil.swapAssetDependencies(clonedAsset, oldToNewIds,
					dependencies);

			final ProjectAsset currentProjectAsset = projectAssetsById.get(clonedAsset.getId());

			final ITerariumAssetService<? extends TerariumAsset> terariumAssetService = terariumAssetServices
					.getServiceByType(currentProjectAsset.getAssetType());

			// persist the clone
			terariumAssetService.createAsset(clonedAsset, Schema.Permission.WRITE);
		}
	}

	public ProjectExport exportProject(final UUID projectId) throws IOException {

		final Project project = projectService.getProject(projectId).orElseThrow();

		final List<ProjectAsset> projectAssets = projectAssetService.getProjectAssets(projectId,
				Schema.Permission.READ);

		final List<AssetExport> exportedAssets = new ArrayList<>();

		for (final ProjectAsset currentProjectAsset : projectAssets) {

			final ITerariumAssetService<? extends TerariumAsset> terariumAssetService = terariumAssetServices
					.getServiceByType(currentProjectAsset.getAssetType());

			final TerariumAsset currentAsset = terariumAssetService
					.getAsset(currentProjectAsset.getId(), Schema.Permission.READ)
					.orElseThrow();

			final Map<String, FileExport> files = terariumAssetService.exportAssetFiles(currentProjectAsset.getId());

			final AssetExport exportedAsset = new AssetExport();
			exportedAsset.setType(currentProjectAsset.getAssetType());
			exportedAsset.setAsset(currentAsset);
			exportedAsset.setFiles(files);
			exportedAssets.add(exportedAsset);
		}

		final ProjectExport projectExport = new ProjectExport();
		projectExport.setProject(project.clone());
		projectExport.setAssets(exportedAssets);
		return projectExport.clone();
	}

	public Project importProject(final ProjectExport export) throws IOException {

		final ProjectExport projectExport = export.clone(); // clone in case it has been imported already

		// create the project
		final Project project = projectService.createProject(projectExport.getProject());

		for (final AssetExport assetExport : projectExport.getAssets()) {

			final AssetType assetType = assetExport.getType();

			final ITerariumAssetService<? extends TerariumAsset> terariumAssetService = terariumAssetServices
					.getServiceByType(assetType);

			// create the asset
			final TerariumAsset asset = terariumAssetService.createAsset(assetExport.getAsset(),
					Schema.Permission.WRITE);

			// upload the files
			for (final Map.Entry<String, FileExport> entry : assetExport.getFiles().entrySet()) {
				final String fileName = entry.getKey();
				final FileExport fileExport = entry.getValue();
				terariumAssetService.uploadFile(asset.getId(), fileName, fileExport.getContentType(),
						fileExport.getBytes());
			}

			// add the asset to the project
			final Optional<ProjectAsset> projectAsset = projectAssetService.createProjectAsset(project, assetType,
					asset, Schema.Permission.WRITE);
			if (projectAsset.isEmpty()) {
				throw new RuntimeException("Failed to create project asset");
			}
		}

		return project;
	}

}
