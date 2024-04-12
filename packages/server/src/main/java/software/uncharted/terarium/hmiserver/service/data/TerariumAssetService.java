package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

/**
 * Base class for services that manage TerariumAssets
 *
 * @param <T> The type of asset this service manages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public abstract class TerariumAssetService<T extends TerariumAsset> implements ITerariumAssetService<T> {

	/** The configuration for the Elasticsearch service */
	protected final ElasticsearchConfiguration elasticConfig;

	/** The configuration for the application */
	protected final Config config;

	/** Services */
	protected final ElasticsearchService elasticService;
	protected final ProjectAssetService projectAssetService;

	/** The class of the asset this service manages */
	private final Class<T> assetClass;

	/**
	 * Get the index for the asset this service manages
	 *
	 * @return The index for the asset this service manages
	 */
	protected abstract String getAssetIndex();

	/**
	 * Get an asset by its ID
	 *
	 * @param id The ID of the asset to get
	 * @return The asset, if it exists
	 * @throws IOException If there is an error retrieving the asset
	 */
	public Optional<T> getAsset(final UUID id) throws IOException {
		final T asset = elasticService.get(getAssetIndex(), id.toString(), assetClass);
		if (asset != null && asset.getDeletedOn() == null) {
			// TODO: This is a hack to fix the fact that the id was not added during the
			// mass es-ingestion
			asset.setId(id);
			return Optional.of(asset);
		}
		return Optional.empty();
	}

	/**
	 * Get a list of assets
	 *
	 * @param page     The page number
	 * @param pageSize The number of assets per page
	 * @param query    The query to filter the assets
	 * @return The list of assets
	 * @throws IOException If there is an error retrieving the assets
	 */
	public List<T> getAssets(final Integer page, final Integer pageSize, final Query query) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(getAssetIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b
						.must(query)
						.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
						.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))
						.mustNot(mn -> mn.term(t -> t.field("isPublic").value(false)))))
				.build();
		return elasticService.search(req, assetClass);
	}

	/**
	 * Get a list of assets
	 *
	 * @param page     The page number
	 * @param pageSize The number of assets per page
	 * @return The list of assets
	 * @throws IOException If there is an error retrieving the assets
	 */
	public List<T> getAssets(final Integer page, final Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(getAssetIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b
						.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
						.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))
						.mustNot(mn -> mn.term(t -> t.field("isPublic").value(false)))))
				.build();
		return elasticService.search(req, assetClass);
	}

	/**
	 * Delete an asset by its ID
	 *
	 * @param id The ID of the asset to delete
	 * @throws IOException If there is an error deleting the asset
	 */
	public Optional<T> deleteAsset(final UUID id) throws IOException {
		final Optional<T> asset = getAsset(id);
		if (asset.isEmpty()) {
			return Optional.empty();
		}
		asset.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateAsset(asset.get());
		return asset;
	}

	/**
	 * Create a new asset and saves to ES
	 *
	 * @param asset The asset to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	public T createAsset(final T asset) throws IOException {
		if (elasticService.documentExists(getAssetIndex(), asset.getId().toString())) {
			throw new IllegalArgumentException("Asset already exists with ID: " + asset.getId());
		}
		asset.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(getAssetIndex(), asset.getId().toString(), asset);
		return asset;
	}

	/**
	 * Create new assets and saves to ES
	 *
	 * @param asset The asset to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	public List<T> createAssets(final List<T> assets) throws IOException {
		for (final T asset : assets) {
			if (elasticService.documentExists(getAssetIndex(), asset.getId().toString())) {
				throw new IllegalArgumentException("Asset already exists with ID: " + asset.getId());
			}
			asset.setCreatedOn(Timestamp.from(Instant.now()));
			elasticService.index(getAssetIndex(), asset.getId().toString(), asset);
		}
		return assets;
	}

	/**
	 * Update an asset and saves to ES
	 *
	 * @param asset The asset to update
	 * @return The updated asset
	 * @throws IOException              If there is an error updating the asset
	 * @throws IllegalArgumentException If the asset tries to move from permanent to
	 *                                  temporary
	 */
	public Optional<T> updateAsset(final T asset) throws IOException, IllegalArgumentException {

		final Optional<T> oldAsset = getAsset(asset.getId());

		if (oldAsset.isEmpty()) {
			return Optional.empty();
		}

		if (asset.getTemporary() && !oldAsset.get().getTemporary()) {
			throw new IllegalArgumentException("Cannot update a non-temporary asset to be temporary");
		}

		asset.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(getAssetIndex(), asset.getId().toString(), asset);

		// Update the related ProjectAsset
		projectAssetService.updateByAsset(asset);

		return Optional.of(asset);
	}

	/**
	 * Clone asset on ES, retrieve and save document with a different id
	 */
	public T cloneAsset(final UUID id) throws IOException, IllegalArgumentException {
		final Optional<T> targetAsset = getAsset(id);
		if (targetAsset.isEmpty()) {
			throw new IllegalArgumentException("Cannot clone non-existent asset: " + id.toString());
		}
		return createAsset(targetAsset.get());
	}
}
