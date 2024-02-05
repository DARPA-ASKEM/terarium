package software.uncharted.terarium.esingest.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Configuration
@ConfigurationProperties(prefix = "terarium.esingest")
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Config {

	List<ElasticIngestParams> ingestParams;
}
