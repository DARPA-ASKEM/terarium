package software.uncharted.terarium.hmiserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableFeignClients
@PropertySource("classpath:application.properties")
public class TerariumApplication {
	public static void main(String[] args) {
		SpringApplication.run(TerariumApplication.class, args);
	}

}
