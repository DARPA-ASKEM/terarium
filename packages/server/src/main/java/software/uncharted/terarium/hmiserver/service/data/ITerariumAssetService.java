package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.FileExport;

public interface ITerariumAssetService<T extends TerariumAsset> {
	Optional<T> getAsset(final UUID id) throws IOException;

	List<T> getPublicNotTemporaryAssets(final Integer page, final Integer pageSize) throws IOException;

	Optional<T> deleteAsset(final UUID id, final UUID projectId) throws IOException;

	T createAsset(final T asset, final UUID projectId) throws IOException;

	List<T> createAssets(final List<T> asset, final UUID projectId) throws IOException;

	Optional<T> updateAsset(final T asset, final UUID projectId) throws IOException;

	void copyAssetFiles(final T newAsset, final T oldAsset) throws IOException;

	Map<String, FileExport> exportAssetFiles(final UUID assetId) throws IOException;

	public Integer uploadFile(
		final UUID assetId,
		final String filename,
		final ContentType contentType,
		final byte[] data
	) throws IOException;

	public Integer uploadFile(final UUID assetId, final String filename, final FileExport fileExport) throws IOException;
}
