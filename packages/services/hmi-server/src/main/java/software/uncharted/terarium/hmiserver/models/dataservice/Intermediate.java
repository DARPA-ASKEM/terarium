package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;

public class Intermediate {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("timestamp")
	public Instant timestamp;

	@JsonbProperty("source")
	public IntermediateSource source;

	@JsonbProperty("type")
	public IntermediateFormat type;

	@JsonbProperty("content")
	public String content;

	public Intermediate(final Instant timestamp, final IntermediateSource source, final IntermediateFormat type, final String content) {
		this.timestamp = timestamp;
		this.source = source;
		this.type = type;
		this.content = content;
	}
}
