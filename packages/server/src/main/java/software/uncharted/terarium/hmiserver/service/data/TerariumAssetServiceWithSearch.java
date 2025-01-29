package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

/**
 * Base class for services that manage TerariumAssets with syncing to
 * Elasticsearch.
 *
 * @param <T> The type of asset this service manages
 * @param <R> The respository of the asset this service manages.
 */
@Service
@Slf4j
public abstract class TerariumAssetServiceWithSearch<
	T extends TerariumAsset, R extends PSCrudSoftDeleteRepository<T, UUID>
>
	extends TerariumAssetServiceWithoutSearch<T, R> {

	public TerariumAssetServiceWithSearch(
		final ObjectMapper objectMapper,
		final Config config,
		final ElasticsearchConfiguration elasticConfig,
		final ElasticsearchService elasticService,
		final EmbeddingService embeddingService,
		final Environment env,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final S3ClientService s3ClientService,
		final R repository,
		final Class<T> assetClass
	) {
		super(objectMapper, config, projectService, projectAssetService, repository, s3ClientService, assetClass);
		this.elasticConfig = elasticConfig;
		this.elasticService = elasticService;
		this.embeddingService = embeddingService;
		this.env = env;
	}

	/** The configuration for the Elasticsearch service */
	protected final ElasticsearchConfiguration elasticConfig;

	/** Services */
	protected final ElasticsearchService elasticService;

	protected final EmbeddingService embeddingService;
	private final Environment env;

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
		final String index = getAssetIndex();
		try {
			final String currentIndex = getCurrentAssetIndex();
			// if another index exists, delete it.
			if (!currentIndex.equals(index)) {
				elasticService.deleteIndex(currentIndex);
			}
		} catch (final Exception e) {}
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
	@Observed(name = "function_profile")
	public List<T> searchAssets(final Integer page, final Integer pageSize, final Query query) throws IOException {
		return searchAssets(page, pageSize, query, null);
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
	@Observed(name = "function_profile")
	public List<T> searchAssets(final Integer page, final Integer pageSize, final Query query, final SourceConfig source)
		throws IOException {
		final SearchRequest.Builder builder = new SearchRequest.Builder().index(getAssetAlias()).from(page).size(pageSize);

		if (query != null) {
			builder.query(query);
		}

		if (source != null) {
			builder.source(source);
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
	@Override
	@Observed(name = "function_profile")
	public Optional<T> deleteAsset(final UUID id, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException {
		final Optional<T> deleted = super.deleteAsset(id, projectId, hasWritePermission);

		if (deleted.isPresent()) {
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
	@Override
	@Observed(name = "function_profile")
	public T createAsset(final T asset, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException {
		final T created = super.createAsset(asset, projectId, hasWritePermission);

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
	@Override
	@Observed(name = "function_profile")
	@SuppressWarnings("unchecked")
	public List<T> createAssets(final List<T> assets, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException {
		final List<T> created = super.createAssets(assets, projectId, hasWritePermission);

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
	@Override
	@Observed(name = "function_profile")
	public Optional<T> updateAsset(final T asset, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException, IllegalArgumentException {
		final Optional<T> updated = super.updateAsset(asset, projectId, hasWritePermission);

		if (updated.isEmpty()) {
			return Optional.empty();
		}

		/** TODO - Asset search needs to be removed properly	
		if (!updated.get().getTemporary() && updated.get().getPublicAsset()) {
			elasticService.index(getAssetAlias(), updated.get().getId().toString(), updated);
		}

		if (updated.get().getTemporary() || !updated.get().getPublicAsset()) {
			elasticService.delete(getAssetAlias(), updated.get().getId().toString());
		}
		*/
		
		return updated;
	}

	/** Upload search vector embeddings into the asset document. */
	@Observed(name = "function_profile")
	public void uploadEmbeddings(
		final UUID assetId,
		final TerariumAssetEmbeddings embeddings,
		final Schema.Permission hasWritePermission
	) throws IOException {
		// Execute the update request
		elasticService.update(getAssetAlias(), assetId.toString(), embeddings);
	}

	private static String getVersionFromIndex(final String index) {
		final String[] split = index.split("_");
		if (split.length < 2) {
			throw new RuntimeException("Unable to parse version from index name: " + index);
		}

		return split[split.length - 1];
	}

	private static String getIndexNameWithoutVersion(final String index) {
		final String[] split = index.split("_");
		if (split.length < 2) {
			throw new RuntimeException("Unable to parse index name: " + index);
		}
		return String.join("_", Arrays.asList(split).subList(0, split.length - 1));
	}

	private static String incrementVersion(final String version) {
		final String[] parts = version.split("\\.");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid version format: " + version);
		}
		int secondPart = Integer.parseInt(parts[1]);
		secondPart++;
		return parts[0] + "." + secondPart;
	}

	private static String generateNextIndexName(final String previousIndex) {
		return (getIndexNameWithoutVersion(previousIndex) + "_" + incrementVersion(getVersionFromIndex(previousIndex)));
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
		if (elasticService.indexExists(newIndexName)) {
			final long count = elasticService.count(newIndexName);
			if (count > 0) {
				throw new RuntimeException(
					"New index " +
					newIndexName +
					" already exists and contains " +
					count +
					" documents, please delete and empty the index"
				);
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

	protected boolean isRunningTestProfile() {
		final String[] activeProfiles = env.getActiveProfiles();

		for (final String profile : activeProfiles) {
			if ("test".equals(profile)) {
				return true;
			}
		}

		return false;
	}

	public Future<Void> generateAndUpsertEmbeddings(final T asset) {
		//TODO: Temporary fix for performance issue. Disabling embeddings for now. Jan 29 2025

		/*if (!isRunningTestProfile() && asset.getPublicAsset() && !asset.getTemporary()) {
			final String embeddingText = asset.getEmbeddingSourceText();
			if (embeddingText == null) {
				return null;
			}

			return CompletableFuture.runAsync(() -> {
				new Thread(() -> {
					try {
						final TerariumAssetEmbeddings embeddings = embeddingService.generateEmbeddings(embeddingText);

						// Execute the update request
						uploadEmbeddings(asset.getId(), embeddings, Schema.Permission.WRITE);
					} catch (final Exception e) {
						log.error("Failed to update embeddings for document {}", asset.getId(), e);
					}
				}).start();
			});
		}*/
		return null;
	}
}
