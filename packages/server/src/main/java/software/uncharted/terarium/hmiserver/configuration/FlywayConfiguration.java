package software.uncharted.terarium.hmiserver.configuration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * This configuration class ensures that hibernate runs BEFORE flyway. This is to preserve the schema generation
 * provided by Hibernate, but still get the benefits of having a proper version migration system in Flyway
 */
@Configuration
@RequiredArgsConstructor
public class FlywayConfiguration {

	private final DataSource dataSource;

	@Value("${flyway.table:flyway_schema_history}")
	private String flywayTableName;

	/**
	 * Do nothing on initialization
	 *
	 * @param flyway
	 * @return
	 */
	@Bean
	FlywayMigrationInitializer flywayInitializer(final Flyway flyway) {
		return new FlywayMigrationInitializer(flyway, f -> {});
	}

	static class FlywayVoid {}

	private String removeJavaMigrationPackage(final String str) {
		final int index = str.indexOf('V');
		if (index != -1) {
			return str.substring(index);
		}
		return str;
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
		Flyway flywayWithCorrectBaseline = flyway;
		if (flywayProperties.isEnabled() && !isFlywayInitialized()) {
			// get all migration names
			final List<String> migrations = new ArrayList<>();
			for (final MigrationInfo migration : flyway.info().all()) {
				migrations.add(removeJavaMigrationPackage(migration.getScript()));
			}
			// generate baseline version
			final String baselineVersion = getBaselineVersion(migrations);

			// re-create the flyway object with the correct baseline
			final FluentConfiguration config = Flyway.configure()
				.configuration(flyway.getConfiguration())
				.baselineVersion(MigrationVersion.fromVersion(baselineVersion));
			flywayWithCorrectBaseline = new Flyway(config);
			flywayWithCorrectBaseline.baseline();
		}
		flywayWithCorrectBaseline.migrate();
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
	private String getBaselineVersion(final List<String> migrations) {
		final String baseline = migrations
			.stream()
			.map(filename -> filename.split("__")[0].substring(1))
			.map(Integer::parseInt)
			.max(Integer::compareTo)
			.orElseThrow()
			.toString();

		return baseline;
	}
}
