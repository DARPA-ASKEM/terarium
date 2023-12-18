package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
public class DatasetService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;
	private final S3ClientService s3ClientService;

	public List<Dataset> getDatasets(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getDocumentIndex())
				.from(page)
				.size(pageSize)
				.build();
		return elasticService.search(req, Dataset.class);
	}

	public Dataset getDataset(UUID id) throws IOException {
		return elasticService.get(elasticConfig.getDocumentIndex(), id.toString(), Dataset.class);
	}

	public void deleteDataset(UUID id) throws IOException {
		elasticService.delete(elasticConfig.getDocumentIndex(), id.toString());
	}

	public Dataset createDataset(Dataset dataset) throws IOException {
		dataset.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDocumentIndex(), dataset.setId(UUID.randomUUID()).getId().toString(),
				dataset);
		return dataset;
	}

	public Optional<Dataset> updateDataset(Dataset dataset) throws IOException {
		if (!elasticService.contains(elasticConfig.getArtifactIndex(), dataset.getId().toString())) {
			return Optional.empty();
		}

		dataset.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDocumentIndex(), dataset.getId().toString(), dataset);
		return Optional.of(dataset);
	}

	private String getPath(UUID documentId, String filename) {
		return String.join("/", config.getDocumentPath(), documentId.toString(), filename);
	}

	public PresignedURL getUploadUrl(UUID documentId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(documentId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public PresignedURL getDownloadUrl(UUID documentId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(documentId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("GET");
		return presigned;
	}
}
