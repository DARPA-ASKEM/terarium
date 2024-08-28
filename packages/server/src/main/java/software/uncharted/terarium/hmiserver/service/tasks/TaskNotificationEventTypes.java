package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.ClientEventType;

@Slf4j
public class TaskNotificationEventTypes {

	private static Map<String, ClientEventType> clientEventTypes = Map.of(
		ModelCardResponseHandler.NAME,
		ClientEventType.TASK_GOLLM_MODEL_CARD,
		ConfigureModelResponseHandler.NAME,
		ClientEventType.TASK_GOLLM_CONFIGURE_MODEL,
		ConfigureFromDatasetResponseHandler.NAME,
		ClientEventType.TASK_GOLLM_CONFIGURE_FROM_DATASET,
		CompareModelsResponseHandler.NAME,
		ClientEventType.TASK_GOLLM_COMPARE_MODEL,
		GenerateResponseHandler.NAME,
		ClientEventType.TASK_GOLLM_GENERATE_SUMMARY,
		ValidateModelConfigHandler.NAME,
		ClientEventType.TASK_FUNMAN_VALIDATION
	);

	public static ClientEventType getTypeFor(String taskName) {
		final ClientEventType eventType = clientEventTypes.get(taskName);
		if (eventType == null) {
			log.warn("Event type not found for task: " + taskName);
			return ClientEventType.TASK_UNDEFINED_EVENT;
		}
		return eventType;
	}
}
