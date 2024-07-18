package db.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import software.uncharted.terarium.hmiserver.models.dataservice.code.CodeFile;

public class V9__CodeFiles extends BaseJavaMigration {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void migrate(final Context context) throws Exception {
		try (var statement = context.getConnection().createStatement()) {
			// Select all rows from the simulation_result_files table
			final ResultSet resultSet = statement.executeQuery("SELECT id, files FROM code ORDER BY id;");

			// Prepare the update statement for the simulation table
			final PreparedStatement preparedStatement = context
				.getConnection()
				.prepareStatement(
					"INSERT INTO code_file (id, created_on, updated_on, dynamics, file_name, language, code_id) VALUES (?, ?, ?, ?::json, ?, ?, ?);"
				);

			// Iterate through the result set
			while (resultSet.next()) {
				// Get the simulation_id and result_files
				final UUID codeId = (UUID) resultSet.getObject("id");
				final String codeFileString = resultSet.getString("files");

				if (codeFileString == null) {
					continue;
				}

				final Map<String, CodeFile> codeFiles = objectMapper.readValue(
					codeFileString,
					new TypeReference<Map<String, CodeFile>>() {}
				);

				for (final Map.Entry<String, CodeFile> entry : codeFiles.entrySet()) {
					final String fileName = entry.getKey();
					final CodeFile file = entry.getValue();
					file.setFileName(fileName);

					final Timestamp createdOn = Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());

					// Set the parameters for the update statement
					preparedStatement.setObject(1, file.getId());
					preparedStatement.setTimestamp(2, createdOn);
					preparedStatement.setTimestamp(3, createdOn);
					preparedStatement.setString(
						4,
						file.getDynamics() != null ? objectMapper.writeValueAsString(file.getDynamics()) : null
					);
					preparedStatement.setString(5, fileName);
					preparedStatement.setObject(6, file.getLanguage() != null ? file.getLanguage().ordinal() : null);
					preparedStatement.setObject(7, codeId);

					// Execute the update statement
					preparedStatement.executeUpdate();
				}
			}

			// Drop the old column
			statement.execute("ALTER TABLE code DROP COLUMN files;");
		}
	}
}
