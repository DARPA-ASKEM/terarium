package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.observation.annotation.Observed;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.repository.data.DatasetRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
public class DatasetService extends TerariumAssetServiceWithSearch<Dataset, DatasetRepository> {

	public DatasetService(
			final ObjectMapper objectMapper,
			final Config config,
			final ElasticsearchConfiguration elasticConfig,
			final ElasticsearchService elasticService,
			final ProjectAssetService projectAssetService,
			final S3ClientService s3ClientService,
			final DatasetRepository repository) {
		super(
				objectMapper,
				config,
				elasticConfig,
				elasticService,
				projectAssetService,
				s3ClientService,
				repository,
				Dataset.class);
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		return config.getDatasetPath();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getDatasetIndex();
	}

	@Override
	public String getAssetAlias() {
		return elasticConfig.getDatasetAlias();
	}

	@Override
	@Observed(name = "function_profile")
	public Dataset createAsset(final Dataset asset, final Schema.Permission hasWritePermission) throws IOException {
		if (asset.getColumns() != null) {
			for (final DatasetColumn column : asset.getColumns()) {
				column.setDataset(asset);
			}
		}
		return super.createAsset(asset, hasWritePermission);
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
	public List<Dataset> createAssets(final List<Dataset> assets, final Schema.Permission hasWritePermission)
			throws IOException {
		for (final Dataset asset : assets) {
			if (asset.getColumns() != null) {
				for (final DatasetColumn column : asset.getColumns()) {
					column.setDataset(asset);
				}
			}
		}

		return super.createAssets(assets, hasWritePermission);
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
	public Optional<Dataset> updateAsset(final Dataset asset, final Schema.Permission hasWritePermission)
			throws IOException, IllegalArgumentException {
		if (asset.getColumns() != null) {
			for (final DatasetColumn column : asset.getColumns()) {
				column.setDataset(asset);
			}
		}
		return super.updateAsset(asset, hasWritePermission);
	}
}
