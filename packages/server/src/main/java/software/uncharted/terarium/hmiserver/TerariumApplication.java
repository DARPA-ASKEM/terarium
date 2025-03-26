package software.uncharted.terarium.hmiserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@PropertySource("classpath:application.properties")
@EnableAsync
@Slf4j
public class TerariumApplication {

	public static void main(final String[] args) {
		SpringApplication.run(TerariumApplication.class, args);
	}
}
