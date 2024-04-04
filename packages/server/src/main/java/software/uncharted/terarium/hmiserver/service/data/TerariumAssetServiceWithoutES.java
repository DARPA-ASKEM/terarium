package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.NotFoundException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

/**
 * Base class for services that manage TerariumAssets without syncing to
 * Elasticsearch.
 *
 * @param <T> The type of asset this service manages
 * @param <R> The respository of the asset this service manages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public abstract class TerariumAssetServiceWithoutES<T extends TerariumAsset, R extends PSCrudSoftDeleteRepository<T, UUID>>
		implements ITerariumAssetService<T> {

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
	public Optional<T> getAsset(final UUID id) {
		return repository.getByIdAndDeletedOnIsNull(id);
	}

	/**
	 * Get a list of assets, this includes all assets, not just searchable ones.
	 *
	 * @param page     The page number
	 * @param pageSize The number of assets per page
	 * @return The list of assets
	 */
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
	public T deleteAsset(final UUID id) throws IOException {
		final Optional<T> asset = getAsset(id);
		if (asset.isEmpty()) {
			throw new NotFoundException("Asset not found for id: " + asset.get().getId().toString());
		}
		asset.get().setDeletedOn(Timestamp.from(Instant.now()));
		repository.save(asset.get());
		return asset.get();
	}

	/**
	 * Create a new asset.
	 *
	 * @param asset The asset to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	public T createAsset(final T asset) throws IOException {
		final UUID id = UUID.randomUUID();
		asset.setId(id);
		asset.setCreatedOn(Timestamp.from(Instant.now()));

		return repository.save(asset);
	}

	/**
	 * Update an asset.
	 *
	 * @param asset The asset to update
	 * @return The updated asset
	 * @throws IOException              If there is an error updating the asset
	 * @throws IllegalArgumentException If the asset tries to move from permanent to
	 *                                  temporary
	 */
	public T updateAsset(final T asset) throws IOException, IllegalArgumentException {

		final Optional<T> oldAsset = getAsset(asset.getId());

		if (oldAsset.isEmpty()) {
			throw new NotFoundException("Asset not found for id: " + asset.getId().toString());
		}

		if (asset.getTemporary() && !oldAsset.get().getTemporary()) {
			throw new IllegalArgumentException("Cannot update a non-temporary asset to be temporary");
		}

		final T updated = repository.save(asset);

		// Update the related ProjectAsset
		projectAssetService.updateByAsset(updated);

		return updated;
	}

	/**
	 * Clone asset, retrieve and save with a different id
	 */
	public T cloneAsset(final UUID id) throws IOException, IllegalArgumentException {
		final Optional<T> targetAsset = getAsset(id);
		if (targetAsset.isEmpty()) {
			throw new IllegalArgumentException("Cannot clone non-existent asset: " + id.toString());
		}
		return createAsset(targetAsset.get());
	}
}
