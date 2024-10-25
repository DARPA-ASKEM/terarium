package software.uncharted.terarium.hmiserver.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "terarium.taskrunner.rabbitmq")
public class TaskRunnerConfiguration {

	public static class RabbitConfig {

		public String addresses;
		public String username;
		public String password;
	}

	private Map<String, RabbitConfig> rabbitmq;

	public Map<String, RabbitConfig> getMap() {
		return rabbitmq;
	}

	public void setMap(final Map<String, RabbitConfig> rabbitmq) {
		this.rabbitmq = rabbitmq;
	}

	@Bean
	public Map<String, RabbitAdmin> rabbitAdminMap() throws URISyntaxException {
		final Map<String, RabbitAdmin> rabbitAdminMap = new HashMap<>();

		for (final Map.Entry<String, RabbitConfig> entry : rabbitmq.entrySet()) {
			final String key = entry.getKey();
			final RabbitConfig rabbitConfig = entry.getValue();

			final URI rabbitAddress = new URI(rabbitConfig.addresses);

			final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
			connectionFactory.setUri(rabbitAddress);
			connectionFactory.setUsername(rabbitConfig.username);
			connectionFactory.setPassword(rabbitConfig.password);

			final RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
			rabbitAdminMap.put(key, rabbitAdmin);
		}

		return rabbitAdminMap;
	}

	public Map<String, RabbitTemplate> rabbitTemplateMap() throws URISyntaxException {
		final Map<String, RabbitTemplate> rabbitTemplateMap = new HashMap<>();

		for (final Map.Entry<String, RabbitConfig> entry : rabbitmq.entrySet()) {
			final String key = entry.getKey();
			final RabbitConfig rabbitConfig = entry.getValue();

			final URI rabbitAddress = new URI(rabbitConfig.addresses);

			final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
			connectionFactory.setUri(rabbitAddress);
			connectionFactory.setUsername(rabbitConfig.username);
			connectionFactory.setPassword(rabbitConfig.password);

			final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
			rabbitTemplateMap.put(key, rabbitTemplate);
		}

		return rabbitTemplateMap;
	}
}
