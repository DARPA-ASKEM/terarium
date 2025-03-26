package software.uncharted.terarium.taskrunner.models.task;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class TaskResponse implements Serializable {

	private UUID id;
	private String script;
	private ProgressState status;
	private byte[] output;
	private Object additionalProperties;
	private UUID projectId;
	protected String userId;
	private String stdout;
	private String stderr;
	private String requestSHA256;
	private String routingKey;
	private boolean useCache = true;
}
