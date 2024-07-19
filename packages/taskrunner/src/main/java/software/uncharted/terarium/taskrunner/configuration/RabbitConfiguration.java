package software.uncharted.terarium.taskrunner.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Slf4j
@Configuration
public class RabbitConfiguration {

	@Value("${spring.rabbitmq.addresses}")
	String rabbitAddresses;

	@Value("${spring.rabbitmq.username}")
	String username;

	@Value("${spring.rabbitmq.password}")
	String password;

	@Bean
	public RabbitAdmin rabbitAdmin() throws URISyntaxException {
		final URI rabbitAddress = new URI(rabbitAddresses);

		log.info("Connecting to RabbitMQ: {}", rabbitAddress);

		final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setUri(rabbitAddress);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);

		connectionFactory.setConnectionListeners(
			Arrays.asList(
				new ConnectionListener() {
					@Override
					public void onCreate(@NonNull final Connection connection) {
						log.info("Successfully created connection to RabbitMQ");
					}

					@Override
					public void onClose(@NonNull final Connection connection) {
						log.warn("Connection to RabbitMQ was closed");
					}

					@Override
					public void onFailed(@NonNull final Exception exception) {
						log.error("Connection to RabbitMQ failed to connect: ", exception);
					}
				}
			)
		);

		return new RabbitAdmin(connectionFactory);
	}
}
