package software.uncharted.terarium.esingest.configuration;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "terarium.elasticsearch")
@Data
@Accessors(chain = true)
public class ElasticsearchConfiguration {
  String url;

  @Value("${terarium.elasticsearch.auth_enabled:false}")
  boolean authEnabled;

  String username;

  String password;

  Index index;

  public record Index(
      String prefix, String suffix, String searchableDocumentRoot, String searchableModelRoot) {}

  public String getIndex(String root) {
    return String.join("_", index.prefix, root, index.suffix);
  }
}
