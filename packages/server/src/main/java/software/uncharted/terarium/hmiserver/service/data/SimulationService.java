package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.service.s3.S3Service;

/**
 * Service class for handling simulations. Note that this does not extend TerariumAssetService, as Simulations do not
 * extend TerariumAsset. This is because simulations have special considerations around their date/time fields when it
 * comes to formatting.
 */
@Service
@RequiredArgsConstructor
public class SimulationService {

	private static final long HOUR_EXPIRATION = 60;
	private final ElasticsearchConfiguration elasticConfig;
	private final ElasticsearchService elasticService;
	private final Config config;
	private final S3ClientService s3ClientService;

	@Observed(name = "function_profile")
	public List<Simulation> getSimulations(final Integer page, final Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getSimulationIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
						.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
				.build();
		return elasticService.search(req, Simulation.class);
	}

	@Observed(name = "function_profile")
	public Optional<Simulation> getSimulation(final UUID id) throws IOException {
		final Simulation doc = elasticService.get(elasticConfig.getSimulationIndex(), id.toString(), Simulation.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	@Observed(name = "function_profile")
	public void deleteSimulation(final UUID id) throws IOException {

		final Optional<Simulation> simulation = getSimulation(id);
		if (simulation.isEmpty()) {
			return;
		}
		simulation.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateSimulation(simulation.get());
	}

	@Observed(name = "function_profile")
	public Simulation createSimulation(final Simulation simulation) throws IOException {
		elasticService.index(
				elasticConfig.getSimulationIndex(),
				simulation.setId(UUID.randomUUID()).getId().toString(),
				simulation);
		return simulation;
	}

	@Observed(name = "function_profile")
	public Optional<Simulation> updateSimulation(final Simulation simulation) throws IOException {
		if (!elasticService.documentExists(
				elasticConfig.getSimulationIndex(), simulation.getId().toString())) {
			return Optional.empty();
		}
		simulation.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(
				elasticConfig.getSimulationIndex(), simulation.getId().toString(), simulation);
		return Optional.of(simulation);
	}

	private String getPath(final UUID id, final String filename) {
		return String.join("/", config.getResultsPath(), id.toString(), filename);
	}

	@Observed(name = "function_profile")
	public PresignedURL getUploadUrl(final UUID id, final String filename) {
		final PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService
				.getS3Service()
				.getS3PreSignedPutUrl(config.getFileStorageS3BucketName(), getPath(id, filename), HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	@Observed(name = "function_profile")
	public Optional<PresignedURL> getDownloadUrl(final UUID id, final String filename) {

		final Optional<String> url = s3ClientService
				.getS3Service()
				.getS3PreSignedGetUrl(config.getFileStorageS3BucketName(), getPath(id, filename), HOUR_EXPIRATION);

		if (url.isEmpty()) {
			return Optional.empty();
		}

		final PresignedURL presigned = new PresignedURL();
		presigned.setUrl(url.get());
		presigned.setMethod("GET");
		return Optional.of(presigned);
	}

	private String getResultsPath(final UUID simId, final String filename) {
		return String.join("/", config.getResultsPath(), simId.toString(), filename);
	}

	private String getDatasetPath(final UUID datasetId, final String filename) {
		return String.join("/", config.getDatasetPath(), datasetId.toString(), filename);
	}

	@Observed(name = "function_profile")
	public void copySimulationResultToDataset(final Simulation simulation, final Dataset dataset) {
		final UUID simId = simulation.getId();
		if (simulation.getResultFiles() != null) {
			for (final String resultFile : simulation.getResultFiles()) {
				final String filename = S3Service.parseFilename(resultFile);
				final String srcPath = getResultsPath(simId, filename);
				final String destPath = getDatasetPath(dataset.getId(), filename);
				s3ClientService
						.getS3Service()
						.copyObject(
								config.getFileStorageS3BucketName(),
								srcPath,
								config.getFileStorageS3BucketName(),
								destPath);
			}
		}
	}
}
