package software.uncharted.terarium.hmiserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableFeignClients
@PropertySource("classpath:application.properties")
@PropertySource(value = "classpath:application-secrets.properties", ignoreResourceNotFound = true)
public class TerariumApplication {
    public static void main(String[] args) {
        SpringApplication.run(TerariumApplication.class, args);
    }

}
