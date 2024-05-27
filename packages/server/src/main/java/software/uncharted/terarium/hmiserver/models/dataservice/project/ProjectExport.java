package software.uncharted.terarium.hmiserver.models.dataservice.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetExport;
import software.uncharted.terarium.hmiserver.utils.AssetDependencyUtil;
import software.uncharted.terarium.hmiserver.utils.AssetDependencyUtil.AssetDependencyMap;

@Data
@Accessors(chain = true)
public class ProjectExport {

	Project project;
	List<AssetExport> assets = new ArrayList<>();

	public ProjectExport clone() {

		final ProjectExport cloned = new ProjectExport();
		cloned.setProject(project.clone());

		// create set of all project asset ids
		final Set<UUID> projectAssetIds = new HashSet<>();
		for (final AssetExport assetExport : assets) {
			projectAssetIds.add(assetExport.getAsset().getId());
		}

		final Map<UUID, AssetDependencyMap> assetDependencies = new HashMap<>();
		final Map<UUID, UUID> oldToNewIds = new HashMap<>();

		final List<AssetExport> clonedAssetExports = new ArrayList<>();

		// determine dependencies for each asset
		for (final AssetExport assetExport : assets) {

			final TerariumAsset currentAsset = assetExport.getAsset();

			// determine any dependencies each asset has
			final AssetDependencyMap dependencies = AssetDependencyUtil.getAssetDependencies(projectAssetIds,
					currentAsset);

			// clone the asset
			final TerariumAsset clonedAsset = currentAsset.clone();

			// store the dependencies
			assetDependencies.put(clonedAsset.getId(), dependencies);

			final AssetExport clonedExport = new AssetExport();
			clonedExport.setType(assetExport.getType());
			clonedExport.setAsset(clonedAsset);
			clonedExport.setFiles(assetExport.getFiles());
			clonedAssetExports.add(clonedExport);

			// store the id mapping
			oldToNewIds.put(currentAsset.getId(), clonedAsset.getId());
		}

		// update all uuids with the cloned uuids
		for (final AssetExport assetExport : clonedAssetExports) {
			final AssetDependencyMap dependencies = assetDependencies.get(assetExport.getAsset().getId());

			// update any referenced dependencies
			final TerariumAsset finalClonedAsset = AssetDependencyUtil.swapAssetDependencies(assetExport.getAsset(),
					oldToNewIds,
					dependencies);

			assetExport.setAsset(finalClonedAsset);
		}

		cloned.setAssets(clonedAssetExports);

		return cloned;
	}
}
