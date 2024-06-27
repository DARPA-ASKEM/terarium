package software.uncharted.terarium.ingest.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.ingest.service.IngestParams;

@Configuration
@ConfigurationProperties(prefix = "terarium")
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Config {

	/**
	 * Ingests configured for the app.
	 */
	List<IngestParams> ingestParams;

	@Data
	static public class HmiServer {
		String url;
	}

	/**
	 * HMI server configuration.
	 */
	HmiServer hmiServer;

}
