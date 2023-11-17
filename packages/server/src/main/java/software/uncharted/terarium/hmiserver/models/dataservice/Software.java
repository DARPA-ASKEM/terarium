package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Software implements Serializable {

	private String id;

	private Instant timestamp;

	private String source;

	@JsonProperty("storage_uri")
	private String storageUri;
}
