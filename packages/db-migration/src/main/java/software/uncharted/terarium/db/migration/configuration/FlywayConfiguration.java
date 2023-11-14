package software.uncharted.terarium.db.migration.configuration;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.uncharted.terarium.db.migration.callbacks.FlywayMigrationCompleteCallback;

import javax.sql.DataSource;
import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
public class FlywayConfiguration {

    private final Config config;
    private final DataSource dataSource;
    private final ApplicationContext applicationContext;

    @Value("${spring.profiles.active:default}")
    private String[] activeProfiles;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
            .dataSource(dataSource)
            .validateMigrationNaming(true)
            .table("migrations")
            .ignoreMigrationPatterns("*:missing")
            .loggers("auto")
            .baselineOnMigrate(true)
            .baselineVersion(MigrationVersion.fromVersion(config.getBaselineVersion()))
            .callbacks(new FlywayMigrationCompleteCallback(applicationContext))
            .locations(getLocations())
            .load();
    }

    private String[] getLocations() {
        final var locations = new ArrayList<String>();
        locations.add("db/migration");

        final var dataPrefix = "db/data";
        for (final String profile : activeProfiles) {
            if (profile.equals("production")) {
                continue;
            }

            locations.add(dataPrefix);
        }

        return locations.toArray(String[]::new);
    }
}
