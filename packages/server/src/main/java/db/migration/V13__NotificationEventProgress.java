package db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V13__NotificationEventProgress extends BaseJavaMigration {

	ObjectMapper objectMapper = new ObjectMapper();

	private String mapToString(final int progress) {
		switch (progress) {
			case 0:
				return "CANCELLED";
			case 1:
				return "COMPLETE";
			case 2:
				return "ERROR";
			case 3:
				return "FAILED";
			case 4:
				return "QUEUED";
			case 5:
				return "RETRIEVING";
			case 6:
				return "RUNNING";
		}
		return null;
	}

	@Override
	public void migrate(final Context context) throws Exception {
		try (var statement = context.getConnection().createStatement()) {
			// Rename transform to transform_old
			statement.execute("ALTER TABLE notification_event RENAME COLUMN state TO state_old;");

			// Create new json column transform
			statement.execute("ALTER TABLE notification_event ADD COLUMN state varchar(255);");

			// Select all rows from the simulation_result_files table
			final ResultSet resultSet = statement.executeQuery(
				"SELECT id, state_old FROM notification_event WHERE state_old IS NOT NULL ORDER BY id;"
			);

			// Prepare the update statement for the simulation table
			final PreparedStatement preparedStatement = context
				.getConnection()
				.prepareStatement("UPDATE notification_event SET state = ? WHERE id = ?;");

			// Iterate through the result set
			while (resultSet.next()) {
				// Get the id and result_files
				final UUID id = (UUID) resultSet.getObject("id");
				final int state = resultSet.getInt("state_old");

				// Set the parameters for the update statement
				preparedStatement.setString(1, mapToString(state));
				preparedStatement.setObject(2, id);

				// Execute the update statement
				preparedStatement.executeUpdate();
			}

			// Create new json column transform
			statement.execute("ALTER TABLE notification_event DROP COLUMN state_old");
		}
	}
}
