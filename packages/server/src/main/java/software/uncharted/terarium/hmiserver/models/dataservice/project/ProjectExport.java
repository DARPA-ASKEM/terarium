package software.uncharted.terarium.hmiserver.models.dataservice.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetExport;
import software.uncharted.terarium.hmiserver.models.dataservice.FileExport;
import software.uncharted.terarium.hmiserver.utils.AssetDependencyUtil;
import software.uncharted.terarium.hmiserver.utils.AssetDependencyUtil.AssetDependencyMap;

@Data
@Accessors(chain = true)
public class ProjectExport {

	Project project;
	List<AssetExport> assets = new ArrayList<>();

	public static byte[] readZipEntry(final ZipInputStream zipInputStream) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int count;
		while ((count = zipInputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, count);
		}
		return baos.toByteArray();
	}

	public void loadFromZipFile(final InputStream inputStream) throws IOException {
		final ObjectMapper objectMapper = new ObjectMapper();

		final ZipInputStream zipInputStream = new ZipInputStream(inputStream);

		// get the project json
		ZipEntry zipEntry = zipInputStream.getNextEntry();
		if (zipEntry == null || !zipEntry.getName().equals("project.json")) {
			throw new IllegalArgumentException("Invalid project export file");
		}

		project = objectMapper.readValue(readZipEntry(zipInputStream), Project.class);

		// iterate on assets
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			// read the asset json
			final AssetExport asset = objectMapper.readValue(readZipEntry(zipInputStream), AssetExport.class);

			// read in the file payloads
			for (final Map.Entry<String, FileExport> entry : asset.getFiles().entrySet()) {
				zipEntry = zipInputStream.getNextEntry();
				if (zipEntry == null) {
					throw new IllegalArgumentException("Invalid project export file, expected a asset file payload");
				}
				final FileExport file = entry.getValue();
				file.setBytes(readZipEntry(zipInputStream));
			}

			assets.add(asset);
		}
	}

	public byte[] getAsZipFile() throws JsonProcessingException, IOException {
		final ObjectMapper objectMapper = new ObjectMapper();

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

		final byte[] projectBytes = objectMapper.writeValueAsBytes(project);

		final ZipEntry zipEntry = new ZipEntry("project.json");
		zipOutputStream.putNextEntry(zipEntry);
		zipOutputStream.write(projectBytes);
		zipOutputStream.closeEntry();

		for (final AssetExport asset : assets) {
			final byte[] assetBytes = objectMapper.writeValueAsBytes(asset);

			final ZipEntry assetEntry = new ZipEntry(asset.getAsset().getId() + ".json");
			zipOutputStream.putNextEntry(assetEntry);
			zipOutputStream.write(assetBytes);
			zipOutputStream.closeEntry();

			for (final Map.Entry<String, FileExport> file : asset.getFiles().entrySet()) {
				final byte[] fileBytes = file.getValue().getBytes();

				final ZipEntry fileEntry = new ZipEntry(asset.getAsset().getId() + "/" + file.getKey());
				zipOutputStream.putNextEntry(fileEntry);
				zipOutputStream.write(fileBytes);
				zipOutputStream.closeEntry();
			}
		}
		zipOutputStream.finish();
		zipOutputStream.close();

		return byteArrayOutputStream.toByteArray();
	}

	@Override
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
			final AssetDependencyMap dependencies = AssetDependencyUtil.getAssetDependencies(projectAssetIds, currentAsset);

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
			final TerariumAsset finalClonedAsset = AssetDependencyUtil.swapAssetDependencies(
				assetExport.getAsset(),
				oldToNewIds,
				dependencies
			);

			assetExport.setAsset(finalClonedAsset);
		}

		cloned.setAssets(clonedAssetExports);

		return cloned;
	}
}
