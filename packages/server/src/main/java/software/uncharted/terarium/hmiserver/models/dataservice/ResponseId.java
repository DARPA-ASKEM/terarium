package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.UUID;

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
public class ResponseId {

	public ResponseId(String id) {
		this.id = id;
	}

	public ResponseId(UUID id) {
		this.id = id.toString();
	}
	//TODO this should be a UUID
	private String id;
}
