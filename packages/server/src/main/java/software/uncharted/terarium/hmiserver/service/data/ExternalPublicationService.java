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
import software.uncharted.terarium.hmiserver.models.dataservice.externalpublication.ExternalPublication;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

/**
 * Service class for handling external publications.
 */
@Service
@RequiredArgsConstructor
public class ExternalPublicationService {

	private final ElasticsearchService elasticService;

	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;

	private final S3ClientService s3ClientService;

	/**
	 * Retrieves an ExternalPublication object by ID.
	 *
	 * @param id The ID of the ExternalPublication to retrieve.
	 * @return The ExternalPublication object if found, or null otherwise.
	 * @throws IOException If an I/O error occurs while retrieving the
	 *                     ExternalPublication.
	 */
	public ExternalPublication getExternalPublication(UUID id) throws IOException {
		return elasticService.get(elasticConfig.getExternalPublicationIndex(), id.toString(),
				ExternalPublication.class);
	}

	/**
	 * Retrieves a list of ExternalPublication objects from an Elasticsearch index.
	 *
	 * @param page     The page number of the search results.
	 * @param pageSize The number of results per page.
	 * @return A list of ExternalPublication objects.
	 * @throws IOException If an I/O error occurs while retrieving the
	 *                     ExternalPublications.
	 */
	public List<ExternalPublication> getExternalPublications(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getExternalPublicationIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn-> mn.exists(e->e.field("deletedOn")))))
				.build();
		return elasticService.search(req, ExternalPublication.class);
	}

	/**
	 * Marks the ExternalPublication as deleted by setting the deletedOn field to the current time
	 *
	 * @param id The ID of the external publication to delete.
	 * @throws IOException If an I/O error occurs while deleting the external
	 *                     publication.
	 */
	public void deleteExternalPublication(UUID id) throws IOException {
		ExternalPublication externalPublication = getExternalPublication(id);
		externalPublication.setDeletedOn(Timestamp.from(Instant.now()));
		updateExternalPublication(externalPublication);
	}

	/**
	 * Creates an external publication by indexing it into Elasticsearch.
	 *
	 * @param externalPublication The external publication to be created.
	 * @return The created external publication.
	 * @throws IOException If an I/O error occurs while indexing the external
	 *                     publication.
	 */
	public ExternalPublication createExternalPublication(ExternalPublication externalPublication) throws IOException {
		elasticService.index(elasticConfig.getExternalPublicationIndex(),
				externalPublication.setId(UUID.randomUUID()).getId().toString(),
				externalPublication);
		return externalPublication;
	}

	/**
	 * Updates an ExternalPublication by indexing it into Elasticsearch.
	 *
	 * @param externalPublication The ExternalPublication object to be updated.
	 * @return The updated ExternalPublication object.
	 * @throws IOException If an I/O error occurs while indexing the
	 *                     ExternalPublication.
	 */
	public Optional<ExternalPublication> updateExternalPublication(ExternalPublication externalPublication)
			throws IOException {
		if (!elasticService.contains(elasticConfig.getExternalPublicationIndex(),
				externalPublication.getId().toString())) {
			return Optional.empty();
		}

		externalPublication.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getExternalPublicationIndex(), externalPublication.getId().toString(),
				externalPublication);
		return Optional.of(externalPublication);
	}

	/**
	 * Calculates the path for a file based on the ID and filename using the
	 * configured document path.
	 *
	 * @param id       The ID of the file.
	 * @param filename The name of the file.
	 * @return The calculated path for the file.
	 */
	private String getPath(UUID id, String filename) {
		return String.join("/", config.getDocumentPath(), id.toString(), filename);
	}

	/**
	 * Generates a pre-signed URL for uploading a file to S3.
	 *
	 * @param id       The ID of the file.
	 * @param filename The name of the file.
	 * @return The generated pre-signed URL.
	 */
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

	/**
	 * Generates a pre-signed URL for downloading a file from S3.
	 *
	 * @param id       The ID of the file.
	 * @param filename The name of the file.
	 * @return The generated pre-signed URL for downloading the file.
	 */
	public PresignedURL getDownloadUrl(UUID id, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(id, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("GET");
		return presigned;
	}

}
