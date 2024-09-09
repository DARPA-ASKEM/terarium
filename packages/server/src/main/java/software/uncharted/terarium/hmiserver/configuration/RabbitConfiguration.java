package software.uncharted.terarium.hmiserver.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
		URI rabbitAddress = new URI(rabbitAddresses);

		final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setUri(rabbitAddress);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		return new RabbitAdmin(connectionFactory);
	}
}
