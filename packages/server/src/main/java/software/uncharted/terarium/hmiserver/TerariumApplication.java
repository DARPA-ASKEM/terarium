package software.uncharted.terarium.hmiserver;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@PropertySource("classpath:application.properties")
public class TerariumApplication {
	public static void main(final String[] args) {
		SpringApplication.run(TerariumApplication.class, args);
	}

	@Value("${server.port:3000}")
	private String serverPort;

	@Value("${server.trustedPort:3001}")
	private String trustedPort;

	@Bean
	public WebServerFactoryCustomizer servletContainer() {
		Connector[] additionalConnectors = this.additionalConnector();

		ServerProperties serverProperties = new ServerProperties();
		return new TomcatMultiConnectorServletWebServerFactoryCustomizer(serverProperties, additionalConnectors);
	}

	private Connector[] additionalConnector() {
		Set<String> defaultPorts = new HashSet<>();
		defaultPorts.add(serverPort);

		if (!defaultPorts.contains(trustedPort)) {
			Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
			connector.setScheme("http");
			connector.setPort(Integer.valueOf(trustedPort));
			return new Connector[]{connector};
		} else {
			return new Connector[]{};
		}
	}

	private class TomcatMultiConnectorServletWebServerFactoryCustomizer extends TomcatServletWebServerFactoryCustomizer {
		private final Connector[] additionalConnectors;

		TomcatMultiConnectorServletWebServerFactoryCustomizer(ServerProperties serverProperties, Connector[] additionalConnectors) {
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
