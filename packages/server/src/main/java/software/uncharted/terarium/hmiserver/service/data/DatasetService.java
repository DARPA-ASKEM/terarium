package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;

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

	public Dataset getDataset(String id) throws IOException {
		return elasticService.get(elasticConfig.getDocumentIndex(), id, Dataset.class);
	}

	public void deleteDataset(String id) throws IOException {
		elasticService.delete(elasticConfig.getDocumentIndex(), id);
	}

	public Dataset createDataset(Dataset dataset) throws IOException {
		elasticService.index(elasticConfig.getDocumentIndex(), dataset.getId(), dataset);
		return dataset;
	}

	public Dataset updateDataset(Dataset dataset) throws IOException {
		elasticService.index(elasticConfig.getDocumentIndex(), dataset.getId(), dataset);
		return dataset;
	}

	private String getPath(String documentId, String filename) {
		return String.join("/", config.getDocumentPath(), documentId, filename);
	}

	public PresignedURL getUploadUrl(String documentId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(documentId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public PresignedURL getDownloadUrl(String documentId, String filename) {
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
