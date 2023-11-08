package software.uncharted.terarium.tds.migration.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "tds")
@Validated
@Data
public class TdsConfig {
    String databaseVersion;
}
