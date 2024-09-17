package software.uncharted.terarium.hmiserver.service;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
public class ImageService {

	final S3ClientService s3ClientService;
	final Config config;

	private static final long EXPIRATION = 60;

	/**
	 * @param id - unique id to identify the image by
	 * @return
	 */
	public String getImageUrl(final UUID id) {
		final String filename = getFilename(id);
		final Optional<String> url = s3ClientService
			.getS3Service()
			.getS3PreSignedGetUrl(config.getFileStorageS3BucketName(), filename, EXPIRATION);
		if (url.isPresent()) {
			return url.get();
		}
		return null;
	}

	/**
	 * @param id - unique id to identify the image by
	 * @param base64Data image encoded in base64, prefixed by
	 */
	public void storeImage(final UUID id, final String base64Data) {
		final String separator = ",";
		if (base64Data.contains(separator)) {
			final String pngBase64 = base64Data.split(separator)[1];
			final byte[] pngBytes = Base64.getMimeDecoder().decode(pngBase64);

			final String bucket = config.getFileStorageS3BucketName();
			final String key = getFilename(id);

			s3ClientService.getS3Service().putObject(bucket, key, pngBytes);
		}
	}

	/** @param id - unique id to identify the image by */
	public void deleteImage(final UUID id) {
		final String bucket = config.getFileStorageS3BucketName();
		final String key = getFilename(id);
		s3ClientService.getS3Service().deleteObject(bucket, key);
	}

	private String getFilename(final UUID id) {
		return String.join("/", config.getImagePath(), id.toString());
	}
}
