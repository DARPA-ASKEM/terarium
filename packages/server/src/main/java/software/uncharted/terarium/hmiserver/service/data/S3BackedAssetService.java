package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for services that manage TerariumAssets that are backed by S3
 * @param <T>
 */
public abstract class S3BackedAssetService<T extends TerariumAsset> extends TerariumAssetService<T>{
	/** The expiration time for the presigned URLs in minutes */
	private final static long EXPIRATION = 60;
	/** The S3 client service */
	private final S3ClientService s3ClientService;

	/**
	 * Create a new S3BackedAssetService
	 * @param elasticConfig The configuration for the Elasticsearch service
	 * @param config The configuration for the application
	 * @param elasticService The Elasticsearch service
	 * @param s3ClientService The S3 client service
	 * @param assetClass The class of the asset this service manages
	 */
	public S3BackedAssetService(final ElasticsearchConfiguration elasticConfig, final Config config, final ElasticsearchService elasticService, final S3ClientService s3ClientService, final Class<T> assetClass) {
		super(elasticConfig, config, elasticService, assetClass);
		this.s3ClientService = s3ClientService;
	}

	/**
	 * Get the path to the asset in S3
	 * @return The path to the asset in S3
	 */
	protected abstract String getAssetPath();

	/**
	 * Get a presigned URL for uploading a file to S3
	 * @param id The ID of the asset to upload to
	 * @param filename The name of the file to upload
	 * @return The presigned URL
	 */
	public PresignedURL getUploadUrl(final UUID id, final String filename) {


		final PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
			config.getFileStorageS3BucketName(),
			getPath(id, filename),
			EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	/**
	 * Get a presigned URL for downloading a file from S3
	 * @param id The ID of the asset to download from
	 * @param filename The name of the file to download
	 * @return The presigned URL
	 */
	public Optional<PresignedURL> getDownloadUrl(final UUID id, final String filename) {

		final Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(
			config.getFileStorageS3BucketName(),
			getPath(id, filename),
			EXPIRATION);

		if (url.isEmpty()) {
			return Optional.empty();
		}

		final PresignedURL presigned = new PresignedURL();
		presigned.setUrl(url.get());
		presigned.setMethod("GET");
		return Optional.of(presigned);
	}


	public ResponseEntity<Void> getUploadStream(final UUID datasetId, final String filename, final MultipartFile file)
		throws IOException {
		final String bucket = config.getFileStorageS3BucketName();
		final String key = getPath(datasetId, filename);

		return s3ClientService.getS3Service().getUploadStream(bucket, key, file);
	}

	public ResponseEntity<StreamingResponseBody> getDownloadStream(final UUID datasetId, final String filename) {
		final String bucket = config.getFileStorageS3BucketName();
		final String key = getPath(datasetId, filename);

		return s3ClientService.getS3Service().getDownloadStream(bucket, key);
	}

	private String getPath(final UUID id, final String filename) {
		return String.join("/", getAssetPath(), id.toString(), filename);
	}
}
