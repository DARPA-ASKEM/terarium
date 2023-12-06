package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.service.S3ClientService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class ArtifactService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;
	private final S3ClientService s3ClientService;

	public Artifact getArtifact(String id) throws IOException {
		return elasticService.get(elasticConfig.getArtifactIndex(), id, Artifact.class);
	}

	public List<Artifact> getArtifacts(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getArtifactIndex())
				.from(page)
				.size(pageSize)
				.build();
		return elasticService.search(req, Artifact.class);
	}

	public void deleteArtifact(String id) throws IOException {
		elasticService.delete(elasticConfig.getArtifactIndex(), id);
	}

	public Artifact createArtifact(Artifact artifact) throws IOException {
		elasticService.index(elasticConfig.getArtifactIndex(), artifact.getId(), artifact);
		return artifact;
	}

	public Artifact updateArtifact(Artifact artifact) throws IOException {
		elasticService.index(elasticConfig.getArtifactIndex(), artifact.getId(), artifact);
		return artifact;
	}

	private String getPath(String documentId, String filename) {
		return String.join("/", config.getArtifactPath(), documentId, filename);
	}

	public PresignedURL getUploadUrl(String documentId, String filename) {
		long HOUR_EXPIRATION = 60 * 24;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(documentId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public PresignedURL getDownloadUrl(String documentId, String filename) {
		long HOUR_EXPIRATION = 60 * 24;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(documentId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("GET");
		return presigned;
	}

}
