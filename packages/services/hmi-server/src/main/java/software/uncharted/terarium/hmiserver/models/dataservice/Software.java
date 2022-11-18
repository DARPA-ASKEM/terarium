package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;

public class Software {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("timestamp")
	public Instant timestamp;

	@JsonbProperty("source")
	public String source;

	@JsonbProperty("storage_uri")
	public String storageUri;

	public Software(final Instant timestamp, final String source, final String storageUri) {
		this.timestamp = timestamp;
		this.source = source;
		this.storageUri = storageUri;
	}
}
