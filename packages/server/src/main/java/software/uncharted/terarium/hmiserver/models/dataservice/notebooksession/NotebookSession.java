package software.uncharted.terarium.hmiserver.models.dataservice.notebooksession;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class NotebookSession extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 9176019416379347233L;

	// TODO: https://github.com/DARPA-ASKEM/terarium/issues/3719
	// private UUID workflowId;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode data;
}
