package software.uncharted.terarium.esingest.configuration;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Configuration
@ConfigurationProperties(prefix = "terarium")
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Config {

  /** Ingests configured for the app. */
  List<ElasticIngestParams> ingestParams;

  /** Amazon configuration */
  Amazon amazon;

  /*
   * S3 Storage related config
   */
  String fileStorageS3BucketName;

  @Data
  @Accessors(chain = true)
  public static class AmazonCredentials {
    String accessKey;
    String secretKey;
  }

  @Data
  @Accessors(chain = true)
  public static class AmazonS3 {
    String region;
    String url;
    String credentialsId;
  }

  @Data
  @Accessors(chain = true)
  public static class Amazon {
    AmazonCredentials credential;
    AmazonS3 s3;
  }
}
