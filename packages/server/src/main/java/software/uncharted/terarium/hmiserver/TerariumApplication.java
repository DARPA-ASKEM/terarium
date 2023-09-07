package software.uncharted.terarium.hmiserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TerariumApplication {
    public static void main(String[] args) {
        SpringApplication.run(TerariumApplication.class, args);
    }

}
