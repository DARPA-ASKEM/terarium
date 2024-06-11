package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.Map;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskNotificationEventTypes {

	private static Map<String, ClientEventType> clientEventTypes = Map.of(
			ModelCardResponseHandler.NAME, ClientEventType.TASK_GOLLM_MODEL_CARD,
			ConfigureModelResponseHandler.NAME, ClientEventType.TASK_GOLLM_CONFIGURE_MODEL,
			ConfigureFromDatasetResponseHandler.NAME, ClientEventType.TASK_GOLLM_CONFIGURE_FROM_DATASET,
			CompareModelsResponseHandler.NAME, ClientEventType.TASK_GOLLM_COMPARE_MODEL,
			ValidateModelConfigHandler.NAME, ClientEventType.TASK_FUNMAN_VALIDATION);

	public static ClientEventType getTypeFor(String taskName) {
		final ClientEventType eventType = clientEventTypes.get(taskName);
		if (eventType == null){
			log.warn("Event type not found for task: " + taskName);
		}
		return eventType == null ? ClientEventType.TASK_UNDEFINED_EVENT : eventType;
	}
}
