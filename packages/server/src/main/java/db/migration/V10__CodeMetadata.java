package db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V10__CodeMetadata extends BaseJavaMigration {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void migrate(final Context context) throws Exception {
		try (var statement = context.getConnection().createStatement()) {
			// Select all rows from the simulation_result_files table
			final ResultSet resultSet = statement.executeQuery(
				"SELECT code_id, metadata, metadata_key FROM code_metadata ORDER BY code_id;"
			);

			// Prepare the update statement for the simulation table
			final PreparedStatement preparedStatement = context
				.getConnection()
				.prepareStatement("UPDATE code SET metadata = ?::json WHERE id = ?;");

			UUID prevId = null;
			Map<String, String> metadataMap = new HashMap<>();

			// Iterate through the result set
			while (resultSet.next()) {
				// Get the code_id and result_files
				final UUID codeId = (UUID) resultSet.getObject("code_id");
				if (prevId == null || !prevId.equals(codeId)) {
					if (prevId != null) {
						// Set the parameters for the update statement
						preparedStatement.setString(1, objectMapper.writeValueAsString(metadataMap));
						preparedStatement.setObject(2, prevId);

						// Execute the update statement
						preparedStatement.executeUpdate();

						metadataMap = new HashMap<>();
					}
					prevId = codeId;
				}

				final String metadata = resultSet.getString("metadata");
				final String key = resultSet.getString("metadata_key");

				metadataMap.put(key, metadata);
			}

			if (prevId != null && !metadataMap.isEmpty()) {
				// Set the parameters for the update statement
				preparedStatement.setString(1, objectMapper.writeValueAsString(metadataMap));
				preparedStatement.setObject(2, prevId);

				// Execute the update statement
				preparedStatement.executeUpdate();
			}

			// Drop the old table
			statement.execute("DROP TABLE IF EXISTS code_metadata;");
		}
	}
}
