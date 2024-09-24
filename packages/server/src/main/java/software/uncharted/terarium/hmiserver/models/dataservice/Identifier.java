package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.Serial;
import java.io.Serializable;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
public record Identifier(String curie, String name) implements Serializable {
	@Serial
	private static final long serialVersionUID = 302308407252037615L;
}
