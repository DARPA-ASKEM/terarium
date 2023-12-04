package software.uncharted.terarium.hmiserver.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.experimental.Accessors;

@Configuration
@ConfigurationProperties(prefix = "terarium.s3")
@Data
@Accessors(chain = true)
public class S3Configuration {

	String bucket;

	@Value("${terarium.s3.documents_path:documents}")
	String documentsPath;
}
