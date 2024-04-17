package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

public interface ITerariumAssetService<T extends TerariumAsset> {

	Optional<T> getAsset(final UUID id) throws IOException;

	List<T> getAssets(final Integer page, final Integer pageSize) throws IOException;

	Optional<T> deleteAsset(final UUID id) throws IOException;

	T createAsset(final T asset) throws IOException;

	List<T> createAssets(final List<T> asset) throws IOException;

	Optional<T> updateAsset(final T asset) throws IOException;

	T cloneAsset(final UUID id) throws IOException, IllegalArgumentException;
}
