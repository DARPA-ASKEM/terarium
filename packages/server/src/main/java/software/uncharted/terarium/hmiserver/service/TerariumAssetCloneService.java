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

	private final ProjectService projectService;
	private final ProjectAssetService projectAssetService;
	private final TerariumAssetServices terariumAssetServices;

	/**
	 * Given a project and a target asset, discover any assets that the target asset
	 * depends on, clone them, replace any
	 * ids with the newly cloned ids and persist them.
	 *
	 * @param projectId
	 * @param assetId
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TerariumAsset> cloneAndPersistAsset(final UUID projectId, final UUID assetId) throws IOException {

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

		final List<TerariumAsset> clonedAssets = new ArrayList<>();

		final Map<UUID, UUID> oldToNewIds = new HashMap<>();
		final Map<UUID, AssetType> assetTypes = new HashMap<>();

		while (assetsToClone.size() > 0) {

			final UUID currentAssetId = assetsToClone.pop();

			if (oldToNewIds.containsKey(currentAssetId)) {
				// already cloned this asset
				continue;
			}

			final ProjectAsset currentProjectAsset = projectAssetsById.get(currentAssetId);

			final ITerariumAssetService terariumAssetService = terariumAssetServices
					.getServiceByType(currentProjectAsset.getAssetType());

			final TerariumAsset currentAsset = (TerariumAsset) terariumAssetService
					.getAsset(currentAssetId, Schema.Permission.READ)
					.orElseThrow();

			final AssetDependencyMap dependencies = AssetDependencyUtil.getAssetDependencies(projectAssetIds,
					currentAsset);

			for (final UUID dependencyId : dependencies.getIds()) {
				assetsToClone.push(dependencyId);
			}

			// clone the asset
			final TerariumAsset clonedAsset = currentAsset.clone();
			clonedAssets.add(clonedAsset);

			// store its dependencies
			assetDependencies.put(clonedAsset.getId(), dependencies);

			// store asset type
			assetTypes.put(clonedAsset.getId(), currentProjectAsset.getAssetType());

			// store the id mapping
			oldToNewIds.put(currentAssetId, clonedAsset.getId());

			// copy the files to new buckets
			terariumAssetService.copyAssetFiles(clonedAsset, currentAsset, Schema.Permission.WRITE);
		}

		// update all uuids with the cloned uuids

		final List<TerariumAsset> res = new ArrayList<>();

		for (final TerariumAsset clonedAsset : clonedAssets) {

			final AssetDependencyMap dependencies = assetDependencies.get(clonedAsset.getId());

			// update any referenced dependencies
			final TerariumAsset resolved = AssetDependencyUtil.swapAssetDependencies(clonedAsset, oldToNewIds,
					dependencies);

			final AssetType assetType = assetTypes.get(clonedAsset.getId());

			final ITerariumAssetService terariumAssetService = terariumAssetServices.getServiceByType(assetType);

			// persist the clone
			final TerariumAsset created = (TerariumAsset) terariumAssetService.createAsset(resolved,
					Schema.Permission.WRITE);

			res.add(created);
		}

		return res;
	}

	/**
	 * Given a project, clone all assets and download all related files. Return
	 * everything as a singular ProjectExport
	 * object.
	 *
	 * @param projectId
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ProjectExport exportProject(final UUID projectId) throws IOException {

		final Project project = projectService.getProject(projectId).orElseThrow();

		final List<ProjectAsset> projectAssets = projectAssetService.getProjectAssets(projectId,
				Schema.Permission.READ);

		final List<AssetExport> exportedAssets = new ArrayList<>();

		for (final ProjectAsset currentProjectAsset : projectAssets) {

			final ITerariumAssetService terariumAssetService = terariumAssetServices
					.getServiceByType(currentProjectAsset.getAssetType());

			final TerariumAsset currentAsset = (TerariumAsset) terariumAssetService
					.getAsset(currentProjectAsset.getAssetId(), Schema.Permission.READ)
					.orElseThrow();

			final Map<String, FileExport> files = terariumAssetService
					.exportAssetFiles(currentProjectAsset.getAssetId(), Schema.Permission.READ);

			final AssetExport exportedAsset = new AssetExport();
			exportedAsset.setType(currentProjectAsset.getAssetType());
			exportedAsset.setAsset(currentAsset);
			exportedAsset.setFiles(files);
			exportedAssets.add(exportedAsset);
		}

		final ProjectExport projectExport = new ProjectExport();
		projectExport.setProject(project.clone());
		projectExport.getProject().setUserId(null); // clear the user id
		projectExport.getProject().setUserName(null); // clear the user name
		projectExport.setAssets(exportedAssets);
		return projectExport.clone();
	}

	/**
	 * Given a ProjectExport object, import the project and all related assets.
	 *
	 * @param export
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Project importProject(final String userId, final String userName, final ProjectExport export)
			throws IOException {

		final ProjectExport projectExport = export.clone(); // clone in case it has been imported already

		// set the current user id
		projectExport.getProject().setUserId(userId);
		projectExport.getProject().setUserName(userName);

		// create the project
		final Project project = projectService.createProject(projectExport.getProject());

		for (final AssetExport assetExport : projectExport.getAssets()) {

			final AssetType assetType = assetExport.getType();

			final ITerariumAssetService terariumAssetService = terariumAssetServices.getServiceByType(assetType);

			// create the asset
			final TerariumAsset asset = (TerariumAsset) terariumAssetService.createAsset(assetExport.getAsset(),
					Schema.Permission.WRITE);

			// upload the files
			for (final Map.Entry<String, FileExport> entry : assetExport.getFiles().entrySet()) {
				final String fileName = entry.getKey();
				final FileExport fileExport = entry.getValue();
				terariumAssetService.uploadFile(
						asset.getId(), fileName, fileExport.getContentType(), fileExport.getBytes());
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
