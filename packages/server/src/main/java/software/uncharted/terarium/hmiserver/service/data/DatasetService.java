package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
				.index(elasticConfig.getDatasetIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b
					.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
					.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
				.build();
		return elasticService.search(req, Dataset.class);
	}

	public Optional<Dataset> getDataset(UUID id) throws IOException {
		Dataset doc = elasticService.get(elasticConfig.getDatasetIndex(), id.toString(), Dataset.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public void deleteDataset(UUID id) throws IOException {
		Optional<Dataset> dataset = getDataset(id);
		if (dataset.isEmpty()) {
			return;
		}
		dataset.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateDataset(dataset.get());
	}

	public Dataset createDataset(Dataset dataset) throws IOException {
		dataset.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDatasetIndex(), dataset.setId(UUID.randomUUID()).getId().toString(),
				dataset);
		return dataset;
	}

	public Optional<Dataset> updateDataset(Dataset dataset) throws IOException {
		if (!elasticService.contains(elasticConfig.getDatasetIndex(), dataset.getId().toString())) {
			return Optional.empty();
		}

		dataset.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDatasetIndex(), dataset.getId().toString(), dataset);
		return Optional.of(dataset);
	}

	private String getPath(UUID datasetId, String filename) {
		return String.join("/", config.getDatasetPath(), datasetId.toString(), filename);
	}

	public PresignedURL getUploadUrl(UUID datasetId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(datasetId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public Optional<PresignedURL> getDownloadUrl(UUID datasetId, String filename) {
		long HOUR_EXPIRATION = 60;

		Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(datasetId, filename),
				HOUR_EXPIRATION);

		if (url.isEmpty()) {
			return Optional.empty();
		}

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(url.get());
		presigned.setMethod("GET");
		return Optional.of(presigned);
	}

	public ResponseEntity<Void> getUploadStream(UUID datasetId, String filename, MultipartFile file)
			throws IOException {
		final String bucket = config.getFileStorageS3BucketName();
		final String key = getPath(datasetId, filename);

		return s3ClientService.getS3Service().getUploadStream(bucket, key, file);
	}

	public ResponseEntity<StreamingResponseBody> getDownloadStream(UUID datasetId, String filename) {
		final String bucket = config.getFileStorageS3BucketName();
		final String key = getPath(datasetId, filename);

		return s3ClientService.getS3Service().getDownloadStream(bucket, key);
	}

}
