package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

/**
 * Base class for services that manage TerariumAssets with syncing to
 * Elasticsearch.
 *
 * @param <T> The type of asset this service manages
 * @param <R> The respository of the asset this service manages
 */
@Service
@Slf4j
public abstract class TerariumAssetServiceWithSearch<T extends TerariumAsset, R extends PSCrudSoftDeleteRepository<T, UUID>>
		extends TerariumAssetServiceWithoutSearch<T, R> {

	public TerariumAssetServiceWithSearch(
			final Config config,
			final ElasticsearchConfiguration elasticConfig,
			final ElasticsearchService elasticService,
			final ProjectAssetService projectAssetService,
			final R repository,
			final Class<T> assetClass) {

		super(config, projectAssetService, repository, assetClass);

		this.elasticConfig = elasticConfig;
		this.elasticService = elasticService;
	}

	/** The configuration for the Elasticsearch service */
	protected final ElasticsearchConfiguration elasticConfig;

	/** Services */
	protected final ElasticsearchService elasticService;

	/**
	 * Get the index for the asset this service manages
	 *
	 * @return The index for the asset this service manages
	 */
	protected abstract String getAssetIndex();

	/**
	 * Get the alias for the asset this service manages
	 *
	 * @return The alias for the asset this service manages
	 */
	public abstract String getAssetAlias();

	/**
	 * Setup the index and alias for the asset this service manages and ensure it is
	 * empty
	 *
	 * @throws IOException If there is an error setting up the index and alias
	 */
	public void setupIndexAndAliasAndEnsureEmpty() throws IOException {
		log.info("Setting up index {} and alias {}", getAssetIndex(), getAssetAlias());
		String index = getAssetIndex();
		try {
			index = getCurrentAssetIndex();
			log.info("Found latest index version {}", index);
		} catch (final Exception e) {
		}
		elasticService.createOrEnsureIndexIsEmpty(index);
		elasticService.createAlias(index, getAssetAlias());
	}

	/**
	 * Teardown the index and alias for the asset this service manages
	 *
	 * @throws IOException If there is an error tearing down the index and alias
	 */
	public void teardownIndexAndAlias() throws IOException {
		log.info("Tearing down index {}", getAssetIndex());
		final String index = getCurrentAssetIndex();
		elasticService.deleteIndex(index);
	}

	/**
	 * Get a list of assets based on a search query. Only searchable assets wil be
	 * returned.
	 *
	 * @param page     The page number
	 * @param pageSize The number of assets per page
	 * @param query    The query to filter the assets
	 * @return The list of assets
	 * @throws IOException If there is an error retrieving the assets
	 */
	public List<T> searchAssets(final Integer page, final Integer pageSize, final Query query) throws IOException {
		final SearchRequest.Builder builder = new SearchRequest.Builder()
				.index(getAssetAlias())
				.from(page)
				.size(pageSize);

		if (query != null) {
			builder.query(query);
		}

		final SearchRequest req = builder.build();
		return elasticService.search(req, assetClass);
	}

	/**
	 * Delete an asset by its ID
	 *
	 * @param id The ID of the asset to delete
	 * @throws IOException If there is an error deleting the asset
	 */
	public Optional<T> deleteAsset(final UUID id) throws IOException {

		final Optional<T> deleted = super.deleteAsset(id);

		if (deleted.isPresent() && !deleted.get().getTemporary() || deleted.get().getPublicAsset()) {
			elasticService.delete(getAssetAlias(), id.toString());
		}

		return deleted;
	}

	/**
	 * Create a new asset.
	 *
	 * @param asset The asset to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	public T createAsset(final T asset) throws IOException {
		final T created = super.createAsset(asset);

		if (created.getPublicAsset() && !created.getTemporary()) {
			elasticService.index(getAssetAlias(), created.getId().toString(), created);
		}

		return created;
	}

	/**
	 * Create new assets.
	 *
	 * @param assets The assets to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	@SuppressWarnings("unchecked")
	public List<T> createAssets(final List<T> assets) throws IOException {
		final List<T> created = super.createAssets(assets);

		if (created.size() > 0) {
			elasticService.bulkIndex(getAssetAlias(), (List<TerariumAsset>) created);
			elasticService.refreshIndex(getAssetAlias());
		}

		return created;
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
	public Optional<T> updateAsset(final T asset) throws IOException, IllegalArgumentException {

		final Optional<T> updated = super.updateAsset(asset);

		if (updated.isEmpty()) {
			return Optional.empty();
		}

		if (!updated.get().getTemporary() && updated.get().getPublicAsset()) {
			elasticService.index(getAssetAlias(), updated.get().getId().toString(), updated);
		}

		return updated;
	}

	private String getVersionFromIndex(final String index) {
		final String[] split = index.split("_");
		if (split.length < 2) {
			throw new RuntimeException("Unable to parse version from index name: " + index);
		}

		return split[split.length - 1];
	}

	private String getIndexNameWithoutVersion(final String index) {
		final String[] split = index.split("_");
		if (split.length < 2) {
			throw new RuntimeException("Unable to parse index name: " + index);
		}
		return String.join("_", Arrays.asList(split).subList(0, split.length - 1));
	}

	private String incrementVersion(final String version) {
		final String[] parts = version.split("\\.");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid version format: " + version);
		}
		int secondPart = Integer.parseInt(parts[1]);
		secondPart++;
		return parts[0] + "." + secondPart;
	}

	private String generateNextIndexName(final String previousIndex) {
		return getIndexNameWithoutVersion(previousIndex) + "_" + incrementVersion(getVersionFromIndex(previousIndex));
	}

	public String getCurrentAssetIndex() throws IOException {
		return elasticService.getIndexFromAlias(getAssetAlias());
	}

	public void syncAllAssetsToNewIndex() throws IOException {
		syncAllAssetsToNewIndex(false);
	}

	// override this if fetching the full assets requires a different query
	protected Page<T> getPage(final int page, final int pageSize) {
		final Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findAllByPublicAssetIsTrueAndTemporaryIsFalseAndDeletedOnIsNull(pageable);
	}

	public void syncAllAssetsToNewIndex(final boolean deleteOldIndexOnSuccess) throws IOException {
		final int PAGE_SIZE = 256;

		final String oldIndexName = getCurrentAssetIndex();
		final String newIndexName = generateNextIndexName(oldIndexName);

		// create the new index
		if (elasticService.containsIndex(newIndexName)) {
			final long count = elasticService.count(newIndexName);
			if (count > 0) {
				throw new RuntimeException("New index " + newIndexName + " already exists and contains " + count
						+ " documents, please delete and empty the index");
			}
		} else {
			elasticService.createIndex(newIndexName);
		}

		int page = 0;
		Page<T> rows;
		do {
			rows = getPage(page, PAGE_SIZE);

			final List<TerariumAsset> assets = new ArrayList<>();
			for (final T asset : rows.getContent()) {
				assets.add(asset);
			}

			if (assets.size() > 0) {
				log.info("Indexing {} assets into new index {}...", assets.size(), newIndexName);
				elasticService.bulkIndex(newIndexName, assets);
			}

			page++;
		} while (rows.hasNext());

		// refresh the index
		elasticService.refreshIndex(newIndexName);

		// transfer the alias
		elasticService.transferAlias(getAssetAlias(), oldIndexName, newIndexName);

		// delete old index
		if (deleteOldIndexOnSuccess) {
			elasticService.deleteIndex(oldIndexName);
		}
	}

	public void updateIndexInPlace() throws IOException {
		final int PAGE_SIZE = 256;

		int page = 0;
		Page<T> rows;
		do {
			final Pageable pageable = PageRequest.of(page, PAGE_SIZE);
			rows = repository.findAllByPublicAssetIsTrueAndTemporaryIsFalseAndDeletedOnIsNull(pageable);

			final List<TerariumAsset> assets = new ArrayList<>();
			for (final T asset : rows.getContent()) {
				assets.add(asset);
			}

			elasticService.bulkUpdate(getAssetAlias(), assets);

			page++;
		} while (rows.hasNext());

		// refresh the index
		elasticService.refreshIndex(getAssetAlias());
	}

	public void migrateOldESDataToSQL() throws IOException {
		final String index = getCurrentAssetIndex();
		final int PAGE_SIZE = 256;
		String lastId = null;

		log.info("Migrating from ES index {} to SQL...", index);

		if (!elasticService.containsIndex(index)) {
			throw new RuntimeException("No index found: " + index);
		}

		final long count = elasticService.count(index);
		if (count == 0) {
			throw new RuntimeException("No assets found in index: " + index);
		}

		final String SORT_FIELD = "createdOn";

		while (true) {
			final SearchRequest.Builder reqBuilder = new SearchRequest.Builder()
					.index(index)
					.size(PAGE_SIZE)
					.sort(new SortOptions.Builder()
							.field(new FieldSort.Builder().field(SORT_FIELD).order(SortOrder.Asc).build()).build());

			if (lastId != null) {
				reqBuilder.searchAfter(FieldValue.of(SORT_FIELD), FieldValue.of(lastId));
			}

			final SearchRequest req = reqBuilder
					.build();

			final SearchResponse<T> resp = elasticService.getClient().search(req, assetClass);

			final String pageLastId = null;
			final List<T> assets = new ArrayList<>();
			for (final Hit<T> hit : resp.hits().hits()) {
				lastId = hit.sort().get(0).toString();

				final T asset = hit.source();
				if (asset == null) {
					log.warn("Null document payload for id: {}, skipping", hit.id());
					continue;
				}
				if (asset.getId() == null || asset.getId().toString() != hit.id()) {
					asset.setId(UUID.fromString(hit.id()));
				}
				assets.add(hit.source());
			}

			if (assets.size() > 0) {
				repository.saveAll(assets);
			}

			if (pageLastId == null || lastId == pageLastId || assets.size() < PAGE_SIZE) {
				break;
			}
		}

		// once we have transfered all the assets from ES to SQL, the ids will be
		// different and we need to re-sync.
		syncAllAssetsToNewIndex();
	}

}
