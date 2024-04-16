package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.NotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

/**
 * Base class for services that manage TerariumAssets without syncing to Elasticsearch.
 *
 * @param <T> The type of asset this service manages
 * @param <R> The respository of the asset this service manages
 */
@Service
@Data
@RequiredArgsConstructor
@Slf4j
public abstract class TerariumAssetServiceWithoutSearch<
				T extends TerariumAsset, R extends PSCrudSoftDeleteRepository<T, UUID>>
		implements ITerariumAssetService<T> {

	protected final ObjectMapper objectMapper = new ObjectMapper();

	/** The configuration for the application */
	protected final Config config;

	protected final ProjectAssetService projectAssetService;

	/** The repository for the asset this service manages */
	protected final R repository;

	/** The class of the asset this service manages */
	protected final Class<T> assetClass;

	/**
	 * Get an asset by its ID
	 *
	 * @param id The ID of the asset to get
	 * @return The asset, if it exists
	 */
	@Override
	@Observed(name = "function_profile")
	public Optional<T> getAsset(final UUID id) {
		return repository.getByIdAndDeletedOnIsNull(id);
	}

	/**
	 * Check if an asset exists by its ID
	 *
	 * @param id
	 * @return
	 */
	@Observed(name = "function_profile")
	public boolean assetExists(final UUID id) {
		return repository.getByIdAndDeletedOnIsNull(id).isPresent();
	}

	/**
	 * Get a list of assets, this includes all assets, not just searchable ones.
	 *
	 * @param page The page number
	 * @param pageSize The number of assets per page
	 * @return The list of assets
	 */
	@Override
	@Observed(name = "function_profile")
	public List<T> getAssets(final Integer page, final Integer pageSize) {
		final Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findAllByDeletedOnIsNull(pageable).getContent();
	}

	/**
	 * Delete an asset by its ID
	 *
	 * @param id The ID of the asset to delete
	 * @throws IOException If there is an error deleting the asset
	 */
	@Override
	@Observed(name = "function_profile")
	public Optional<T> deleteAsset(final UUID id) throws IOException {
		final Optional<T> asset = getAsset(id);
		if (asset.isEmpty()) {
			return Optional.empty();
		}
		asset.get().setDeletedOn(Timestamp.from(Instant.now()));
		repository.save(asset.get());
		return asset;
	}

	/**
	 * Create a new asset.
	 *
	 * @param asset The asset to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	@Override
	@Observed(name = "function_profile")
	public T createAsset(final T asset) throws IOException {
		if (assetExists(asset.getId())) {
			throw new IllegalArgumentException("Asset already exists for id:" + asset.getId());
		}
		return repository.save(asset);
	}

	/**
	 * Create new assets.
	 *
	 * @param assets The assets to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	@Override
	@Observed(name = "function_profile")
	public List<T> createAssets(final List<T> assets) throws IOException {
		final List<UUID> ids = assets.stream().map(TerariumAsset::getId).toList();
		final List<T> existing = repository.findAllByIdInAndDeletedOnIsNull(ids);
		if (existing.size() > 0) {
			throw new IllegalArgumentException("Asset already exists for id:"
					+ ids.stream().map(UUID::toString).toList());
		}
		return repository.saveAll(assets);
	}

	/**
	 * Update an asset.
	 *
	 * @param asset The asset to update
	 * @return The updated asset
	 * @throws IOException If there is an error updating the asset
	 * @throws IllegalArgumentException If the asset tries to move from permanent to temporary
	 */
	@Override
	@Observed(name = "function_profile")
	public Optional<T> updateAsset(final T asset) throws IOException, IllegalArgumentException {

		final Optional<T> oldAsset = getAsset(asset.getId());

		if (oldAsset.isEmpty()) {
			throw new NotFoundException(
					"Asset not found for id: " + asset.getId().toString());
		}

		if (asset.getTemporary() && !oldAsset.get().getTemporary()) {
			throw new IllegalArgumentException("Cannot update a non-temporary asset to be temporary");
		}

		final T updated = repository.save(asset);

		// Update the related ProjectAsset
		projectAssetService.updateByAsset(updated);

		return Optional.of(updated);
	}

	/** Clone asset and return it, does not persist it. */
	@Override
	@Observed(name = "function_profile")
	@SuppressWarnings("unchecked")
	public T cloneAsset(final UUID id) throws IOException, IllegalArgumentException {
		final Optional<T> targetAsset = getAsset(id);
		if (targetAsset.isEmpty()) {
			throw new IllegalArgumentException("Cannot clone non-existent asset: " + id.toString());
		}
		return (T) targetAsset.get().clone();
	}

	/** Clone asset, write it to the db under a new id, and return it. */
	@Observed(name = "function_profile")
	public T cloneAndPersistAsset(final UUID id) throws IOException, IllegalArgumentException {
		return createAsset(cloneAsset(id));
	}

	/** Returns the asset as a byte payload. */
	@Observed(name = "function_profile")
	public byte[] exportAsset(final UUID id) {
		try {
			return objectMapper.writeValueAsBytes(cloneAsset(id));
		} catch (final Exception e) {
			throw new RuntimeException("Failed to export asset", e);
		}
	}

	/** Imports the asset from a byte payload. */
	@Observed(name = "function_profile")
	public T importAsset(final byte[] bytes) {
		try {
			final T asset = objectMapper.readValue(bytes, assetClass);
			if (assetExists(asset.getId())) {
				throw new RuntimeException("Asset already exists for id:" + asset.getId());
			}
			return createAsset(asset);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to export asset", e);
		}
	}
}
