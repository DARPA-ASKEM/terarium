package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.FileExport;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

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
	@Override
	@Observed(name = "function_profile")
	public Optional<T> getAsset(final UUID id, final Schema.Permission hasReadPermission) throws IOException {
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
	 * @param page The page number
	 * @param pageSize The number of assets per page
	 * @param query The query to filter the assets
	 * @return The list of assets
	 * @throws IOException If there is an error retrieving the assets
	 */
	@Observed(name = "function_profile")
	public List<T> getAssets(final Integer page, final Integer pageSize, final Query query) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(getAssetIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.must(query)
						.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
						.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))
						.mustNot(mn -> mn.term(t -> t.field("isPublic").value(false)))))
				.build();
		return elasticService.search(req, assetClass);
	}

	/**
	 * Get a list of assets
	 *
	 * @param page The page number
	 * @param pageSize The number of assets per page
	 * @return The list of assets
	 * @throws IOException If there is an error retrieving the assets
	 */
	@Override
	@Observed(name = "function_profile")
	public List<T> getPublicNotTemporaryAssets(final Integer page, final Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(getAssetIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
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
	@Override
	@Observed(name = "function_profile")
	public Optional<T> deleteAsset(final UUID id, final Schema.Permission hasWritePermission) throws IOException {
		final Optional<T> asset = getAsset(id, hasWritePermission);
		if (asset.isEmpty()) {
			return Optional.empty();
		}
		asset.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateAsset(asset.get(), hasWritePermission);
		return asset;
	}

	/**
	 * Create a new asset and saves to ES
	 *
	 * @param asset The asset to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	@Override
	@Observed(name = "function_profile")
	public T createAsset(final T asset, final Schema.Permission hasWritePermission) throws IOException {
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
	 * @param assets The assets to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	@Override
	@Observed(name = "function_profile")
	public List<T> createAssets(final List<T> assets, final Schema.Permission hasWritePermission) throws IOException {
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
	 * @throws IOException If there is an error updating the asset
	 * @throws IllegalArgumentException If the asset tries to move from permanent to temporary
	 */
	@Override
	@Observed(name = "function_profile")
	public Optional<T> updateAsset(final T asset, final Schema.Permission hasWritePermission)
			throws IOException, IllegalArgumentException {

		final Optional<T> oldAsset = getAsset(asset.getId(), hasWritePermission);

		if (oldAsset.isEmpty()) {
			return Optional.empty();
		}

		if (asset.getTemporary() && !oldAsset.get().getTemporary()) {
			throw new IllegalArgumentException("Cannot update a non-temporary asset to be temporary");
		}

		asset.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(getAssetIndex(), asset.getId().toString(), asset);

		// Update the related ProjectAsset
		projectAssetService.updateByAsset(asset, hasWritePermission);

		return Optional.of(asset);
	}

	@Observed(name = "function_profile")
	public void copyAssetFiles(final T newAsset, final T oldAsset, final Schema.Permission hasWritePermission)
			throws IOException {

		throw new UnsupportedOperationException("Unimplemented");
	}

	@Observed(name = "function_profile")
	public Map<String, FileExport> exportAssetFiles(final UUID assetId, final Schema.Permission hasReadPermission) {

		throw new UnsupportedOperationException("Unimplemented");
	}
}
