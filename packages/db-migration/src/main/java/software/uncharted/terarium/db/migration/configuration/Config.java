package software.uncharted.terarium.db.migration.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "db.migration")
@Validated
@Data
public class Config {
    String baselineVersion;
}
