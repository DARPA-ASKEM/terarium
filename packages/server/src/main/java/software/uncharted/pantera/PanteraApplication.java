package software.uncharted.pantera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PanteraApplication {
    public static void main(String[] args) {
        SpringApplication.run(PanteraApplication.class, args);
    }

}
