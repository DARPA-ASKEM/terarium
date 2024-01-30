package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationResult;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
public class SimulationService {

	private final ElasticsearchConfiguration elasticConfig;
	private final ElasticsearchService elasticService;

	private final Config config;
	private final S3ClientService s3ClientService;

	private final long HOUR_EXPIRATION = 60;

	public List<Simulation> getSimulations(final Integer page, final Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getSimulationIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b
					.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
					.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
				.build();
		return elasticService.search(req, Simulation.class);
	}

	public Optional<Simulation> getSimulation(final UUID id) throws IOException {
		Simulation doc = elasticService.get(elasticConfig.getSimulationIndex(), id.toString(), Simulation.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public void deleteSimulation(final UUID id) throws IOException {

		Optional<Simulation> simulation = getSimulation(id);
		if (simulation.isEmpty()) {
			return;
		}
		simulation.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateSimulation(simulation.get());
	}

	public Simulation createSimulation(final Simulation simulation) throws IOException {
		elasticService.index(elasticConfig.getSimulationIndex(), simulation.setId(UUID.randomUUID()).getId().toString(),
				simulation);
		return simulation;
	}

	public Optional<Simulation> updateSimulation(final Simulation simulation) throws IOException {
		if (!elasticService.contains(elasticConfig.getSimulationIndex(), simulation.getId().toString())) {
			return Optional.empty();
		}
		simulation.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getSimulationIndex(), simulation.getId().toString(), simulation);
		return Optional.of(simulation);
	}

	private String getPath(final UUID id, final String filename) {
		return String.join("/", config.getResultsPath(), id.toString(), filename);
	}

	public PresignedURL getUploadUrl(final UUID id, final String filename) {
		final PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(id, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public Optional<PresignedURL> getDownloadUrl(UUID id, String filename) {
		long HOUR_EXPIRATION = 60;

		Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(id, filename),
				HOUR_EXPIRATION);

		if (url.isEmpty()) {
			return Optional.empty();
		}

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(url.get());
		presigned.setMethod("GET");
		return Optional.of(presigned);
	}

	private String getResultsPath(UUID simId, String filename) {
		return String.join("/", config.getResultsPath(), simId.toString(), filename);
	}

	private String getDatasetPath(UUID datasetId, String filename) {
		return String.join("/", config.getDatasetPath(), datasetId.toString(), filename);
	}

	public Dataset copySimulationResultToDataset(Simulation simulation) {
		UUID simId = simulation.getId();
		String simName = simulation.getName();

		Dataset dataset = new Dataset();
		dataset.setName(simName + " Result Dataset");
		dataset.setDescription(simulation.getDescription());
		dataset.setMetadata(Map.of("simulationId", simId));
		dataset.setFileNames(
				simulation.getResultFiles().stream().map(f -> f.getFilename()).toList());
		dataset.setDataSourceDate(simulation.getCompletedTime());
		dataset.setColumns(new ArrayList<>());

		// Attach the user to the dataset
		if (simulation.getUserId() != null) {
			dataset.setUserId(simulation.getUserId());
		}

		if (simulation.getResultFiles() != null) {
			for (SimulationResult result : simulation.getResultFiles()) {
				String resultFile = result.getFilename();
				String filename = s3ClientService.getS3Service().parseFilename(resultFile);
				String srcPath = getResultsPath(simId, filename);
				String destPath = getDatasetPath(dataset.getId(), filename);

				s3ClientService.getS3Service().copyObject(config.getFileStorageS3BucketName(), srcPath,
						config.getFileStorageS3BucketName(), destPath);
			}
		}

		return dataset;
	}
}
