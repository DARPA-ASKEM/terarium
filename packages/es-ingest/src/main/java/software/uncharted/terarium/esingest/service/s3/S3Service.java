package software.uncharted.terarium.esingest.service.s3;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.uncharted.terarium.esingest.configuration.Config;
import software.uncharted.terarium.esingest.configuration.Config.Amazon;

@Slf4j
public class S3Service {

  private S3Client client;

  public S3Service(Amazon config) {
    final Config.AmazonS3 s3Config = config.getS3();
    final Config.AmazonCredentials credentials = config.getCredential();
    final AwsCredentials creds =
        StaticCredentialsProvider.create(
                AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey()))
            .resolveCredentials();

    if (s3Config.getUrl() != null && !s3Config.getUrl().isEmpty()) {
      client =
          S3Client.builder()
              .credentialsProvider(StaticCredentialsProvider.create(creds))
              .region(Region.of(s3Config.getRegion()))
              .forcePathStyle(true)
              .endpointOverride(URI.create(s3Config.getUrl()))
              .build();
    } else {
      client =
          S3Client.builder()
              .credentialsProvider(StaticCredentialsProvider.create(creds))
              .region(Region.of(s3Config.getRegion()))
              .build();
    }
  }

  public void destroy() {
    client.close();
  }

  public PutObjectResponse putObject(final String bucketName, final String key, final byte[] data) {
    log.debug("Putting object {} in bucket {}", key, bucketName);
    final PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();
    return client.putObject(request, RequestBody.fromBytes(data));
  }

  public boolean objectExists(final String bucketName, final String key) {
    log.debug("Checking if object {} exists in bucket {}", key, bucketName);
    final HeadObjectRequest request =
        HeadObjectRequest.builder().bucket(bucketName).key(key).build();
    try {
      client.headObject(request);
      log.debug("Object {} exists in bucket {}", key, bucketName);
      return true;
    } catch (NoSuchKeyException e) {
      log.debug("Object {} does not exist in bucket {}", key, bucketName);
      return false;
    }
  }
}
