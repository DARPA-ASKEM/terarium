package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.Map;
import software.uncharted.terarium.hmiserver.models.ClientEventType;

public class TaskNotificationEventTypes {

	private static Map<String, ClientEventType> clientEventTypes = Map.of(
			ModelCardResponseHandler.NAME, ClientEventType.TASK_GOLLM_MODEL_CARD,
			// ConfigureModelResponseHandler.NAME, ClientEventType.TASK_GOLLM_CONFIGURE_MODEL,
			// ConfigureFromDatasetResponseHandler.NAME, ClientEventType.TASK_GOLLM_DATASET_CONFIGURE,
			// CompareModelsResponseHandler.NAME, ClientEventType.TASK_GOLLM_COMPARE_MODEL
			// Add more task names and their corresponding event types here
			);

	public static ClientEventType getTypeFor(String taskName) {
		final ClientEventType eventType = clientEventTypes.get(taskName);
		return eventType == null ? ClientEventType.TASK_UNDEFINED_EVENT : eventType;
	}
}
