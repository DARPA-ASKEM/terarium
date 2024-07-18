package db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V8__SimulationResultFiles extends BaseJavaMigration {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void migrate(final Context context) throws Exception {
		try (var statement = context.getConnection().createStatement()) {
			// Check the type of the transform column
			final ResultSet typeResultSet = statement.executeQuery(
				"SELECT data_type FROM information_schema.columns " +
				"WHERE table_name = 'simulation' AND column_name = 'result_files';"
			);

			if (typeResultSet.next()) {
				final String dataType = typeResultSet.getString("data_type");
				if (!"json".equals(dataType)) {
					// type is not `json`
					statement.execute("ALTER TABLE simulation ALTER COLUMN result_files TYPE json USING result_files::json;");
				}
			}

			// Select all rows from the simulation_result_files table
			final ResultSet resultSet = statement.executeQuery(
				"SELECT simulation_id, result_files FROM simulation_result_files ORDER BY simulation_id;"
			);

			// Prepare the update statement for the simulation table
			final PreparedStatement preparedStatement = context
				.getConnection()
				.prepareStatement("UPDATE simulation SET result_files = ?::json WHERE id = ?;");

			UUID prevId = null;
			List<String> resultFiles = new ArrayList<>();

			// Iterate through the result set
			while (resultSet.next()) {
				// Get the simulation_id and result_files
				final UUID simulationId = (UUID) resultSet.getObject("simulation_id");
				if (prevId == null || !prevId.equals(simulationId)) {
					if (prevId != null) {
						// Set the parameters for the update statement
						preparedStatement.setString(1, objectMapper.writeValueAsString(resultFiles));
						preparedStatement.setObject(2, prevId);

						// Execute the update statement
						preparedStatement.executeUpdate();

						resultFiles = new ArrayList<>();
					}
					prevId = simulationId;
				}

				resultFiles.add(resultSet.getString("result_files"));
			}

			if (prevId != null && !resultFiles.isEmpty()) {
				// Set the parameters for the update statement
				preparedStatement.setString(1, objectMapper.writeValueAsString(resultFiles));
				preparedStatement.setObject(2, prevId);

				// Execute the update statement
				preparedStatement.executeUpdate();
			}

			statement.execute("DROP TABLE IF EXISTS simulation_result_files;");
		}
	}
}
