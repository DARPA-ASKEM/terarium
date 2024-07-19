package software.uncharted.terarium.taskrunner.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "terarium")
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Config {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	/**
	 * If queues should be declared durable. IF running Rabbit inside docker, this
	 * should be false
	 */
	Boolean durableQueues = false;
}
