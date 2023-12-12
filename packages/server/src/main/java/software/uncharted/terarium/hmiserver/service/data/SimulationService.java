package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationService {

	private final ElasticsearchConfiguration elasticConfig;
	private final ElasticsearchService elasticService;

	private final Config config;
	private final S3ClientService s3ClientService;

	private final long HOUR_EXPIRATION = 60 * 24;

	public List<Simulation> getSimulations(final Integer page, final Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
			.index(elasticConfig.getSimulationIndex())
			.from(page)
			.size(pageSize)
			.build();
		return elasticService.search(req, Simulation.class);
	}

	public Simulation getSimulation(final String id) throws IOException {
		return elasticService.get(elasticConfig.getSimulationIndex(), id, Simulation.class);
	}

	public void deleteSimulation(final String id) throws IOException {
		elasticService.delete(elasticConfig.getSimulationIndex(), id);
	}

	public Simulation createSimulation(final Simulation simulation) throws IOException {
		elasticService.index(elasticConfig.getSimulationIndex(), simulation.getId(), simulation);
		return simulation;
	}

	public Simulation updateSimulation(final String id, final Simulation simulation) throws IOException, IllegalArgumentException {
		if (!id.equals(simulation.getId())) {
			throw new IllegalArgumentException("Simulation ID does not match Simulation object ID");
		}
		elasticService.index(elasticConfig.getSimulationIndex(), simulation.getId(), simulation);
		return simulation;
	}

	private String getPath(final String id, final String filename) {
		return String.join("/", config.getResultsPath(), id, filename);
	}

	public PresignedURL getUploadUrl(final String id, final String filename) {
		final PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
			config.getFileStorageS3BucketName(),
			getPath(id, filename),
			HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public PresignedURL getDownloadUrl(final String id, final String filename) {
		final PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedGetUrl(
			config.getFileStorageS3BucketName(),
			getPath(id, filename),
			HOUR_EXPIRATION));
		presigned.setMethod("GET");
		return presigned;
	}
}
