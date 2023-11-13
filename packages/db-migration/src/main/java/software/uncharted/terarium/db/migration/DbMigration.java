package software.uncharted.terarium.db.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@Slf4j
public class DbMigration {
	public static void main(final String[] args) {
  	try {
			SpringApplication.run(DbMigration.class, args);
		} catch (final Exception e) {
			System.exit(1);
		}
	}
}
