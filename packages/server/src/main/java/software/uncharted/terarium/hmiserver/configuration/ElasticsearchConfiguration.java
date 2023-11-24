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

	public record Index (
		String prefix,
		String suffix,
		String codeRoot,
		String documentRoot,
		String equationRoot,
		String modelRoot,
		String simulationRoot,
		String workflowRoot
	) {}

	public String getCodeIndex() {
		return String.join("_", index.prefix, index.codeRoot, index.suffix);
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

	public String getSimulationIndex() {
		return String.join("_", index.prefix, index.simulationRoot, index.suffix);
	}

	public String getWorkflowIndex() {
		return String.join("_", index.prefix, index.workflowRoot, index.suffix);
	}
}
