package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
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
public abstract class TerariumAssetServiceWithES<T extends TerariumAsset, R extends PSCrudSoftDeleteRepository<T, UUID>>
		extends TerariumAssetServiceWithoutES<T, R> {

	public TerariumAssetServiceWithES(
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

		final SearchRequest req = new SearchRequest.Builder()
				.index(getAssetAlias())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b
						.must(query)))
				.build();
		return elasticService.search(req, assetClass);
	}

	/**
	 * Delete an asset by its ID
	 *
	 * @param id The ID of the asset to delete
	 * @throws IOException If there is an error deleting the asset
	 */
	public T deleteAsset(final UUID id) throws IOException {

		final T deleted = super.deleteAsset(id);

		if (!deleted.getTemporary() || deleted.getPublicAsset()) {
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
	 * Update an asset.
	 *
	 * @param asset The asset to update
	 * @return The updated asset
	 * @throws IOException              If there is an error updating the asset
	 * @throws IllegalArgumentException If the asset tries to move from permanent to
	 *                                  temporary
	 */
	public T updateAsset(final T asset) throws IOException, IllegalArgumentException {

		final T updated = super.updateAsset(asset);

		if (!updated.getTemporary() && updated.getPublicAsset()) {
			elasticService.index(getAssetAlias(), updated.getId().toString(), updated);
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
			final Pageable pageable = PageRequest.of(page, PAGE_SIZE);
			rows = repository.findAllByPublicAssetIsTrueAndTemporaryIsFalseAndDeletedOnIsNull(pageable);

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
	}

}
