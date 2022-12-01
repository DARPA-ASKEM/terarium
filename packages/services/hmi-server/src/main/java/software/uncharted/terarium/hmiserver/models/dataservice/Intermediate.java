package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Intermediate implements Serializable {

	private String id;

	private Instant timestamp;

	private IntermediateSource source;

	private IntermediateFormat type;

	private String content;
}

enum IntermediateFormat {
	BILAYER("bilayer"),
	GROMET("gromet"),
	OTHER("other"),
	SBML("sbml");

	public final String type;

	IntermediateFormat(final String type) {
		this.type = type;
	}
}

enum IntermediateSource {
	MREPRESENTATIONA("mrepresentationa"),
	SKEMA("skema");

	public final String type;

	IntermediateSource(final String type) {
		this.type = type;
	}
}
