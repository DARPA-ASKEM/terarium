package software.uncharted.terarium.tds.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@Slf4j
public class TdsMigration {
    public static void main(final String[] args) {
        SpringApplication.run(TdsMigration.class, args);
    }
}
