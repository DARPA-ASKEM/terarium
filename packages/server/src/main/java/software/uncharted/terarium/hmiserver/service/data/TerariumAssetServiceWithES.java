package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
		extends TerariumAssetServiceWithoutES<T, R> implements ITerariumAssetService<T> {

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
				.index(getAssetIndex())
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
			elasticService.delete(getAssetIndex(), id.toString());
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
			elasticService.index(getAssetIndex(), created.getId().toString(), created);
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
			elasticService.index(getAssetIndex(), updated.getId().toString(), updated);
		}

		return updated;
	}

}
