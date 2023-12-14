package software.uncharted.terarium.hmiserver.models.dataservice.notebooksession;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
public class NotebookSession {

	@TSOptional
	private UUID id;

	@TSOptional
	private Instant timestamp;

	private String name;

	@TSOptional
	private String description;

	private Object data;

}
