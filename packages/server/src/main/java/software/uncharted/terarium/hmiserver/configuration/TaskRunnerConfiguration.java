package software.uncharted.terarium.hmiserver.configuration;

import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "terarium.taskrunner")
@Data
public class TaskRunnerConfiguration {

	@Data
	public static class RabbitConfig {

		private String addresses;
		private String username;
		private String password;
	}

	private Map<String, RabbitConfig> rabbitmq;
}
