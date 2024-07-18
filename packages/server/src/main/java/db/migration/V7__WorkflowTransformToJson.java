package db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.hibernate.internal.util.SerializationHelper;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Transform;

public class V7__WorkflowTransformToJson extends BaseJavaMigration {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void migrate(final Context context) throws Exception {
		try (var statement = context.getConnection().createStatement()) {
			// Check the type of the transform column
			final ResultSet typeResultSet = statement.executeQuery(
				"SELECT data_type FROM information_schema.columns " +
				"WHERE table_name = 'workflow' AND column_name = 'transform';"
			);

			if (typeResultSet.next()) {
				final String dataType = typeResultSet.getString("data_type");
				if (!"bytea".equals(dataType)) {
					// type is not `bytea`, skip this migration
					return;
				}
			}

			// Rename transform to transform_old
			statement.execute("ALTER TABLE workflow RENAME COLUMN transform TO transform_old;");

			// Create new json column transform
			statement.execute("ALTER TABLE workflow ADD COLUMN transform json;");

			// Select all rows from the table
			final ResultSet resultSet = statement.executeQuery("SELECT id, transform_old FROM workflow;");

			// Prepare the update statement
			final PreparedStatement preparedStatement = context
				.getConnection()
				.prepareStatement("UPDATE workflow SET transform = ?::json WHERE id = ?;");

			// Iterate through the result set
			while (resultSet.next()) {
				// Get the id and transform_old
				final UUID id = (UUID) resultSet.getObject("id");
				final byte[] transformOld = resultSet.getBytes("transform_old");

				if (transformOld == null) {
					continue;
				}

				final Transform transform = (Transform) SerializationHelper.deserialize(transformOld);

				final String transformJson = objectMapper.writeValueAsString(transform);

				// Update the transform column
				preparedStatement.setString(1, transformJson);
				preparedStatement.setObject(2, id);
				preparedStatement.executeUpdate();
			}

			// Drop old json column
			statement.execute("ALTER TABLE workflow DROP COLUMN transform_old;");
		}
	}
}
