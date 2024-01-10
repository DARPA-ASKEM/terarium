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
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
public class ArtifactService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;
	private final S3ClientService s3ClientService;

	public Optional<Artifact> getArtifact(UUID id) throws IOException {
		Artifact doc = elasticService.get(elasticConfig.getArtifactIndex(), id.toString(), Artifact.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public List<Artifact> getArtifacts(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getArtifactIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))))
				.build();
		return elasticService.search(req, Artifact.class);
	}

	public void deleteArtifact(UUID id) throws IOException {
		Optional<Artifact> artifact = getArtifact(id);
		if (artifact.isEmpty()) {
			return;
		}
		artifact.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateArtifact(artifact.get());

	}

	public Artifact createArtifact(Artifact artifact) throws IOException {
		artifact.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getArtifactIndex(), artifact.setId(UUID.randomUUID()).getId().toString(),
				artifact);
		return artifact;
	}

	public Optional<Artifact> updateArtifact(Artifact artifact) throws IOException {
		if (!elasticService.contains(elasticConfig.getArtifactIndex(), artifact.getId().toString())) {
			return Optional.empty();
		}

		artifact.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getArtifactIndex(), artifact.getId().toString(), artifact);
		return Optional.of(artifact);
	}

	private String getPath(UUID artifactId, String filename) {
		return String.join("/", config.getArtifactPath(), artifactId.toString(), filename);
	}

	public PresignedURL getUploadUrl(UUID id, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(id, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public Optional<PresignedURL> getDownloadUrl(UUID artifactId, String filename) {
		long HOUR_EXPIRATION = 60;

		Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(artifactId, filename),
				HOUR_EXPIRATION);

		if (url.isEmpty()) {
			return Optional.empty();
		}

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(url.get());
		presigned.setMethod("GET");
		return Optional.of(presigned);
	}

}
