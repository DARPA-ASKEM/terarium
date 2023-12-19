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
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
public class CodeService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;
	private final S3ClientService s3ClientService;

	/**
	 * Retrieves a list of Code objects from an Elasticsearch index.
	 *
	 * @param page     The page number of the search results.
	 * @param pageSize The number of results per page.
	 * @return A ResponseEntity containing a list of Code objects.
	 * @throws IOException if an error occurs while retrieving the Code objects.
	 */
	public List<Code> getCode(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getCodeIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))))
				.build();
		return elasticService.search(req, Code.class);
	}

	/**
	 * Retrieves a Code object from an Elasticsearch index.
	 *
	 * @param id The id of the Code object to retrieve.
	 * @return A ResponseEntity containing a Code object.
	 * @throws IOException if an error occurs while retrieving the Code object.
	 */
	public Optional<Code> getCode(UUID id) throws IOException {
		Code doc = elasticService.get(elasticConfig.getCodeIndex(), id.toString(), Code.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	/**
	 * Deletes a Code object from an Elasticsearch index.
	 *
	 * @param id The id of the Code object to delete.
	 * @throws IOException if an error occurs while deleting the Code object.
	 */
	public void deleteCode(UUID id) throws IOException {
		Optional<Code> code = getCode(id);
		if (code.isEmpty()) {
			return;
		}
		code.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateCode(code.get());
	}

	/**
	 * Creates a Code object in an Elasticsearch index.
	 *
	 * @param code The Code object to create.
	 * @return A ResponseEntity containing the created Code object.
	 * @throws IOException if an error occurs while creating the Code object.
	 */
	public Code createCode(Code code) throws IOException {
		code.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getCodeIndex(), code.setId(UUID.randomUUID()).getId().toString(), code);
		return code;
	}

	/**
	 * Updates a Code object in an Elasticsearch index.
	 *
	 * @param code The Code object to update.
	 * @return A ResponseEntity containing the updated Code object.
	 * @throws IOException if an error occurs while updating the Code object.
	 */
	public Optional<Code> updateCode(Code code) throws IOException {
		if (!elasticService.contains(elasticConfig.getCodeIndex(), code.getId().toString())) {
			return Optional.empty();
		}
		code.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getCodeIndex(), code.getId().toString(), code);
		return Optional.of(code);
	}

	/**
	 * Retrieves the path to a file in S3.
	 *
	 * @param codeId   The id of the Code object.
	 * @param filename The name of the file.
	 * @return The path to the file in S3.
	 */
	private String getPath(UUID codeId, String filename) {
		return String.join("/", config.getCodePath(), codeId.toString(), filename);
	}

	/**
	 * Retrieves a presigned URL for uploading a file to S3.
	 *
	 * @param codeId   The id of the Code object.
	 * @param filename The name of the file.
	 * @return A presigned upload URL.
	 */
	public PresignedURL getUploadUrl(UUID codeId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(codeId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	/**
	 * Retrieves a presigned URL for downloading a file from S3.
	 *
	 * @param codeId   The id of the Code object.
	 * @param filename The name of the file.
	 * @return A presigned download URL.
	 */
	public PresignedURL getDownloadUrl(UUID codeId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(codeId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("GET");
		return presigned;
	}

}
