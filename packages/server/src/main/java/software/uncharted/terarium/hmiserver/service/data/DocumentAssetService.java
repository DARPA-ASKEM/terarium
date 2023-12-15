package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
public class DocumentAssetService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;
	private final S3ClientService s3ClientService;

	public List<DocumentAsset> getDocumentAssets(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getDocumentIndex())
				.from(page)
				.size(pageSize)
				.build();
		return elasticService.search(req, DocumentAsset.class);
	}

	public DocumentAsset getDocumentAsset(UUID id) throws IOException {
		return elasticService.get(elasticConfig.getDocumentIndex(), id.toString(), DocumentAsset.class);
	}

	public void deleteDocumentAsset(UUID id) throws IOException {
		elasticService.delete(elasticConfig.getDocumentIndex(), id.toString());
	}

	public DocumentAsset createDocumentAsset(DocumentAsset document) throws IOException {
		document.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDocumentIndex(), document.getId().toString(), document);
		return document;
	}

	public DocumentAsset updateDocumentAsset(DocumentAsset document) throws IOException {
		elasticService.index(elasticConfig.getDocumentIndex(), document.getId().toString(), document);
		return document;
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

	public String getDocumentDoi(Document doc) {
		String docIdentifier = "";
		if (doc != null && doc.getIdentifier() != null && doc.getIdentifier().size() > 0) {
			for (Map<String, String> identifier : doc.getIdentifier()) {
				if (identifier.get("type").equals("doi")) {
					docIdentifier = identifier.get("id");
					break;
				}
			}
		}
		return docIdentifier;
	}

}
