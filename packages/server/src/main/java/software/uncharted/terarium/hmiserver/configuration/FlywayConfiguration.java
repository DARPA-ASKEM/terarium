package software.uncharted.terarium.hmiserver.configuration;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

/**
 * This configuration class ensures that hibernate runs BEFORE flyway. This is to preserve the schema generation provided
 * by Hibernate, but still get the benefits of having a proper version migration system in Flyway
 */
@Configuration
@RequiredArgsConstructor
public class FlywayConfiguration {

	private final DataSource dataSource;

	@Value("${flyway.table:flyway_schema_history}")
	private String flywayTableName;

	@Value("classpath:${spring.flyway.locations:db/migration}/*.sql")
	Resource[] migrations;

	/**
	 * Set the baseline version to be the latest script.  In the case where flyway is not initialized, we assume
	 * hibernate correctly sets up the database
	 */
	@Bean
	FlywayConfigurationCustomizer customizeBaselineVersion() {
		return configuration -> configuration.baselineVersion(getBaselineVersion());
	}

	/**
	 * Do nothing on initialization
	 *
	 * @param flyway
	 * @return
	 */
	@Bean
	FlywayMigrationInitializer flywayInitializer(final Flyway flyway) {
		return new FlywayMigrationInitializer(flyway, (f) -> {
		});
	}

	static class FlywayVoid {
	}

	/**
	 * Once the entityManagerFactory (aka, Hibernate) has been created it's safe to run migrations
	 *
	 * @param flyway
	 * @param flywayProperties
	 * @return
	 */
	@Bean
	@DependsOn("entityManagerFactory")
	FlywayVoid delayedFlywayInitializer(final Flyway flyway, final FlywayProperties flywayProperties) {
		if (flywayProperties.isEnabled() && !isFlywayInitialized()) {
			flyway.baseline();
		}
		flyway.migrate();
		return new FlywayVoid();
	}

	/**
	 * Tests if flyway is initialized by checking if the schema is present in the database
	 *
	 * @return true if flyway is initialized, false otherwise
	 */
	private boolean isFlywayInitialized() {
		try (final Connection connection = dataSource.getConnection()) {
			final DatabaseMetaData metadata = connection.getMetaData();
			final ResultSet result = metadata.getTables(null, null, flywayTableName, null);
			return result.next();
		} catch (final SQLException e) {
			throw new RuntimeException("Failed to check if Flyway is initialized", e);
		}
	}

	/**
	 * Gets the largest version migration value
	 *
	 * @return the largest (latest) version migration number as a string
	 */
	private String getBaselineVersion() {
		return Arrays.stream(migrations)
			.map(Resource::getFilename)
			.filter(Objects::nonNull)
			.map(filename -> filename.split("__")[0].substring(1))
			.map(Integer::parseInt)
			.max(Integer::compareTo)
			.orElseThrow()
			.toString();
	}
}
