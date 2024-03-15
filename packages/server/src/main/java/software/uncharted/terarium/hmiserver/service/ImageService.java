package software.uncharted.terarium.hmiserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    final S3ClientService s3ClientService;
    final Config config;

    private final static long EXPIRATION = 60;

    /**
     *
     * @param id - unique id to identify the image by
     * @return
     */
    public String getImageUrl(UUID id) {
        final String filename = getFilename(id);
        final Optional<String> url = s3ClientService.getS3Service().getS3PreSignedGetUrl(config.getFileStorageS3BucketName(), filename, EXPIRATION);
        if (url.isPresent()) {
            return url.get();
        }
        return null;
    }

    /**
     *
     * @param id - unique id to identify the image by
     * @param base64Data image encoded in base64, prefixed by
     */
    public void storeImage(UUID id, String base64Data) {
        String separator = ",";
        if (base64Data.contains(separator)) {
            final String pngBase64 = base64Data.split(separator)[1];
            final byte[] pngBytes = Base64.getMimeDecoder().decode(pngBase64);

            final String bucket = config.getFileStorageS3BucketName();
            final String key = getFilename(id);

            s3ClientService.getS3Service().putObject(bucket, key, pngBytes);
        }
    }

		/**
		 *
		 * @param id - unique id to identify the image by
		 */
		public void deleteImage(UUID id) {
				final String bucket = config.getFileStorageS3BucketName();
				final String key = getFilename(id);
				s3ClientService.getS3Service().deleteObject(bucket, key);
		}

    private String getFilename(UUID id) {
        return String.join("/image", id.toString());
    }
}
