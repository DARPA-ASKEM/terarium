package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for services that manage TerariumAssets
 * @param <T> The type of asset this service manages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public abstract class TerariumAssetService<T extends TerariumAsset> {

	/** The configuration for the Elasticsearch service */
	protected final ElasticsearchConfiguration elasticConfig;

	/** The configuration for the application */
	protected final Config config;

	/** The Elasticsearch service */
	protected final ElasticsearchService elasticService;

	/** The ProjectAssetService */
	protected final ProjectAssetService projectAssetService;

	/** The class of the asset this service manages */
	private final Class<T> assetClass;


	/**
	 * Get the index for the asset this service manages
	 * @return The index for the asset this service manages
	 */
	protected abstract String getAssetIndex();


	/**
	 * Get an asset by its ID
	 * @param id The ID of the asset to get
	 * @return The asset, if it exists
	 * @throws IOException If there is an error retrieving the asset
	 */
	public Optional<T> getAsset(final UUID id) throws IOException {
		final T asset = elasticService.get(getAssetIndex(), id.toString(), assetClass);
		if (asset != null && asset.getDeletedOn() == null) {
			return Optional.of(asset);
		}
		return Optional.empty();
	}

	/**
	 * Get a list of assets
	 * @param page The page number
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
				.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
			.build();
		return elasticService.search(req, assetClass);
	}

	/**
	 * Delete an asset by its ID
	 * @param id The ID of the asset to delete
	 * @throws IOException If there is an error deleting the asset
	 */
	public void deleteAsset(final UUID id) throws IOException {
		final Optional<T> asset = getAsset(id);
		if (asset.isEmpty()) {
			return;
		}
		asset.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateAsset(asset.get());

	}

	/**
	 * Create a new asset and saves to ES
	 * @param asset The asset to create
	 * @return The created asset
	 * @throws IOException If there is an error creating the asset
	 */
	public T createAsset(final T asset) throws IOException {
		final UUID id = UUID.randomUUID();
		asset.setId(id);
		asset.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(getAssetIndex(), asset.getId().toString(), asset);
		return asset;
	}


	/**
	 * Update an asset and saves to ES
	 * @param asset The asset to update
	 * @return The updated asset
	 * @throws IOException If there is an error updating the asset
	 * @throws IllegalArgumentException If the asset tries to move from permanent to temporary
	 */
	public Optional<T> updateAsset(final T asset) throws IOException, IllegalArgumentException {

		final Optional<T> oldAsset = getAsset(asset.getId());

		if(oldAsset.isEmpty()) {
			return Optional.empty();
		}

		if(asset.getTemporary() && !oldAsset.get().getTemporary()) {
			throw new IllegalArgumentException("Cannot update a non-temporary asset to be temporary");
		}

		asset.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(getAssetIndex() , asset.getId().toString(), asset);

		// Update the related ProjectAsset.assetName
		final Optional<ProjectAsset> projectAsset = projectAssetService.getProjectAssetById(asset.getId());
		if (projectAsset.isPresent()) {
			final String assetName = getAssetName(asset);
			if (assetName != null) {
				projectAsset.get().setAssetName(assetName);
				projectAssetService.save(projectAsset.get());
			} else {
				log.info("Could not update the project asset name for asset with id: " + asset.getId() + " because the asset name is null");
			}
		}

		return Optional.of(asset);
	}

	/**
	 * Get the name of an asset
	 * @param asset
	 * @return assetName
	 * @param <T>
	 */
	private static <T> String getAssetName(T asset) {
		// Force cast to the correct type because Terrarium Asset does not provide a bloody name
		if (asset instanceof Dataset) {
			return ((Dataset) asset).getName();
		} else if (asset instanceof Model) {
			return ((Model) asset).getHeader().getName();
		} else if (asset instanceof Workflow) {
			return ((Workflow) asset).getName();
		} else if (asset instanceof Code) {
			return ((Code) asset).getName();
		} else if (asset instanceof DocumentAsset) {
			return ((DocumentAsset) asset).getName();
		}
		return null;
	}
}
