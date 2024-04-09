package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import software.uncharted.terarium.hmiserver.models.TerariumAsset;

public interface ITerariumAssetService<T extends TerariumAsset> {

	public Optional<T> getAsset(final UUID id) throws IOException;

	public List<T> getAssets(final Integer page, final Integer pageSize) throws IOException;

	public Optional<T> deleteAsset(final UUID id) throws IOException;

	public T createAsset(final T asset) throws IOException;

	public Optional<T> updateAsset(final T asset) throws IOException;

	public T cloneAsset(final UUID id) throws IOException, IllegalArgumentException;
}
