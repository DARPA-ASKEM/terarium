package software.uncharted.terarium.hmiserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@PropertySource("classpath:application.properties")
@Slf4j
public class TerariumApplication {
	public static void main(final String[] args) {
		try {
			SpringApplication.run(TerariumApplication.class, args);
			log.info("Terarium HMI-Server started.");
		} catch (final Exception e) {
			System.exit(1);
		}
	}
}
