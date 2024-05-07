package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

public interface ITerariumAssetService<T extends TerariumAsset> {

	Optional<T> getAsset(final UUID id, final Schema.Permission hasReadPermission) throws IOException;

	List<T> getPublicNotTemporaryAssets(final Integer page, final Integer pageSize) throws IOException;

	Optional<T> deleteAsset(final UUID id, final Schema.Permission hasWritePermission) throws IOException;

	T createAsset(final T asset, final Schema.Permission hasWritePermission) throws IOException;

	List<T> createAssets(final List<T> asset, final Schema.Permission hasWritePermission) throws IOException;

	Optional<T> updateAsset(final T asset, final Schema.Permission hasWritePermission) throws IOException;

	T cloneAsset(final UUID id, final Schema.Permission hasWritePermission) throws IOException, IllegalArgumentException;
}
