package software.uncharted.terarium.hmiserver;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.TomcatServletWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import software.uncharted.terarium.hmiserver.configuration.Config;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@PropertySource("classpath:application.properties")
public class TerariumApplication {

	@Autowired
	Config config;

	public static void main(final String[] args) {
		SpringApplication.run(TerariumApplication.class, args);
	}

	@Bean
	public WebServerFactoryCustomizer<?> servletContainer() {
		Connector[] additionalConnectors = this.additionalTrustedPortConnector();

		ServerProperties serverProperties = new ServerProperties();
		return new TomcatMultiConnectorServletWebServerFactoryCustomizer(serverProperties, additionalConnectors);
	}

	private Connector[] additionalTrustedPortConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(config.getTrustedPort());
		return new Connector[] { connector };
	}

	private class TomcatMultiConnectorServletWebServerFactoryCustomizer
			extends TomcatServletWebServerFactoryCustomizer {
		private final Connector[] additionalConnectors;

		TomcatMultiConnectorServletWebServerFactoryCustomizer(ServerProperties serverProperties,
				Connector[] additionalConnectors) {
			super(serverProperties);
			this.additionalConnectors = additionalConnectors;
		}

		@Override
		public void customize(TomcatServletWebServerFactory factory) {
			super.customize(factory);

			if (additionalConnectors != null && additionalConnectors.length > 0) {
				factory.addAdditionalTomcatConnectors(additionalConnectors);
			}
		}
	}
}
