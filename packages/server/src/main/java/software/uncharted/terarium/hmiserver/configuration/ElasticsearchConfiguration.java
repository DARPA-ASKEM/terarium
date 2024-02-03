package software.uncharted.terarium.hmiserver.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.experimental.Accessors;

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
			String externalPublicationRoot,
			String decapodesConfigurationRoot,
			String decapodesContext) {
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

	public String getExternalPublicationIndex() {
		return String.join("_", index.prefix, index.externalPublicationRoot, index.suffix);
	}

	public String getDecapodesConfigurationIndex() {
		return String.join("_", index.prefix, index.decapodesConfigurationRoot, index.suffix);
	}

	public String getDecapodesContext() {
		return String.join("_", index.prefix, index.decapodesContext, index.suffix);
	}

}
