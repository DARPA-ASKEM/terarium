package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
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
				.query(q -> q.bool(b -> b
					.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
					.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
				.build();
		return elasticService.search(req, DocumentAsset.class);
	}

	public Optional<DocumentAsset> getDocumentAsset(UUID id) throws IOException {
		DocumentAsset doc = elasticService.get(elasticConfig.getDocumentIndex(), id.toString(), DocumentAsset.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public void deleteDocumentAsset(UUID id) throws IOException {
		Optional<DocumentAsset> document = getDocumentAsset(id);
		if (document.isEmpty()) {
			return;
		}
		document.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateDocumentAsset(document.get());
	}

	public DocumentAsset createDocumentAsset(DocumentAsset document) throws IOException {
		document.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDocumentIndex(), document.setId(UUID.randomUUID()).getId().toString(),
				document);
		return document;
	}

	public Optional<DocumentAsset> updateDocumentAsset(DocumentAsset document) throws IOException {
		if (!elasticService.contains(elasticConfig.getDocumentIndex(), document.getId().toString())) {
			return Optional.empty();
		}

		document.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDocumentIndex(), document.getId().toString(), document);
		return Optional.of(document);
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

	public Optional<PresignedURL> getDownloadUrl(UUID documentId, String filename) {
		long HOUR_EXPIRATION = 60;

		Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(documentId, filename),
				HOUR_EXPIRATION);

		if (url.isEmpty()) {
			return Optional.empty();
		}

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(url.get());
		presigned.setMethod("GET");
		return Optional.of(presigned);
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
