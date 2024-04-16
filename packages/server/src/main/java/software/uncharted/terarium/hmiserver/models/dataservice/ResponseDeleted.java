package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
@Accessors(chain = true)
public class ResponseDeleted {

	public ResponseDeleted(String type, String id) {
		message = String.format("%s successfully deleted: %s", type, id);
	}

	public ResponseDeleted(String type, UUID id) {
		message = String.format("%s successfully deleted: %s", type, id.toString());
	}

	private String message;
}
