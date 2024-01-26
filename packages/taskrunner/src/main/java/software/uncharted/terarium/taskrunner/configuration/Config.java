package software.uncharted.terarium.taskrunner.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Configuration
@ConfigurationProperties(prefix = "terarium")
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Config {

	/**
	 * If queues should be declared durable. IF running Rabbit inside docker, this
	 * should be false
	 */
	Boolean durableQueues = false;

}
