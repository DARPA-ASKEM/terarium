package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

/**
 * Generic response from the dataservice, containing just the id from the
 * asset type which was created/modified/deleted.
 */
@Data
@TSModel
@Accessors(chain = true)
public class ResponseDeleted {

	public ResponseDeleted(String type, String id) {
		message = String.format("%s successfully deleted: %s", type, id);
	}

	private String message;
}
