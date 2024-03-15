package software.uncharted.terarium.hmiserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

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
        final int index = base64Data.indexOf(',');
        if (index > -1 && index + 1 < base64Data.length()) {
            final String pngBase64 = base64Data.substring(index + 1);
            final byte[] pngBytes = Base64.getDecoder().decode(pngBase64);

            final String bucket = config.getFileStorageS3BucketName();
            final String key = getFilename(id);

            s3ClientService.getS3Service().putObject(bucket, key, pngBytes);
        }
    }

    private String getFilename(UUID id) {
        return String.join("/image", id.toString());
    }
}
