package software.uncharted.terarium.hmiserver.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@TSModel
@Accessors(chain = true)
@NoArgsConstructor
@Data
public class ClientLog {

	private String level;
	private long timestampMillis;
	private String message;

	@TSOptional
	private String[] args;
}
