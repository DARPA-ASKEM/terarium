package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Software implements Serializable {

	private String id;

	private Instant timestamp;

	private String source;

	@JsonbProperty("storage_uri")
	private String storageUri;
}
