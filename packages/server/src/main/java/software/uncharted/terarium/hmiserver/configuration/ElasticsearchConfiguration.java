package software.uncharted.terarium.hmiserver.configuration;

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

  public String getIndex(final String root) {
    return String.join("_", index.prefix, root, index.suffix);
  }

  public String getCodeIndex() {
    return String.join("_", index.prefix, index.codeRoot, index.suffix);
  }

  public String getArtifactIndex() {
    return String.join("_", index.prefix, index.artifactRoot, index.suffix);
  }

  public String getDatasetIndex() {
    return String.join("_", index.prefix, index.datasetRoot, index.suffix);
  }

  public String getDocumentIndex() {
    return String.join("_", index.prefix, index.documentRoot, index.suffix);
  }

  public String getEquationIndex() {
    return String.join("_", index.prefix, index.equationRoot, index.suffix);
  }

  public String getModelIndex() {
    return String.join("_", index.prefix, index.modelRoot, index.suffix);
  }

  public String getModelConfigurationIndex() {
    return String.join("_", index.prefix, index.modelConfigurationRoot, index.suffix);
  }

  public String getNotebookSessionIndex() {
    return String.join("_", index.prefix, index.notebookSessionRoot, index.suffix);
  }

  public String getSimulationIndex() {
    return String.join("_", index.prefix, index.simulationRoot, index.suffix);
  }

  public String getWorkflowIndex() {
    return String.join("_", index.prefix, index.workflowRoot, index.suffix);
  }

  public String getDecapodesConfigurationIndex() {
    return String.join("_", index.prefix, index.decapodesConfigurationRoot, index.suffix);
  }

  public String getDecapodesContextIndex() {
    return String.join("_", index.prefix, index.decapodesContextRoot, index.suffix);
  }

  public record Index(
      String prefix,
      String suffix,
      String codeRoot,
      String artifactRoot,
      String datasetRoot,
      String documentRoot,
      String equationRoot,
      String modelRoot,
      String modelConfigurationRoot,
      String notebookSessionRoot,
      String simulationRoot,
      String workflowRoot,
      String decapodesConfigurationRoot,
      String decapodesContextRoot) {}
}
