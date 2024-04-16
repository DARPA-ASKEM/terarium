package software.uncharted.terarium.hmiserver.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serial;
import java.sql.Timestamp;
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
}
