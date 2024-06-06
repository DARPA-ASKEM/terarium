package software.uncharted.terarium.hmiserver.models;

import java.io.Serial;
import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class DataMigration {

	public enum MigrationState {
		SUCCESS,
		FAILED
	}

	@Serial
	private static final long serialVersionUID = 235234532542455464L;

	@Id
	private String tableName;

	private MigrationState state;
	private Timestamp timestamp;

	private Integer totalDocuments;
	private Integer successfulDocuments;
	private Integer failedDocuments;
}
