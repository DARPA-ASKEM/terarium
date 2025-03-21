package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.InterventionService;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterventionsFromDatasetResponseHandler extends LlmTaskResponseHandler {

	public static final String NAME = "gollm:interventions_from_dataset";

	private final ObjectMapper objectMapper;
	private final InterventionService interventionService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input extends LlmTaskResponseHandler.Input {

		@JsonProperty("dataset")
		List<String> dataset;

		@JsonProperty("amr")
		String amr;
	}

	@Data
	public static class Response {

		JsonNode response;
	}

	@Data
	public static class Properties {

		UUID projectId;
		UUID datasetId;
		UUID modelId;
		UUID workflowId;
		UUID nodeId;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final Response interventionPolicies = objectMapper.readValue(resp.getOutput(), Response.class);

			// For each intervention, create a new intervention
			for (final JsonNode policy : interventionPolicies.response.get("interventionPolicies")) {
				final InterventionPolicy ip = objectMapper.treeToValue(policy, InterventionPolicy.class);

				if (ip.getModelId() != props.modelId) {
					ip.setModelId(props.modelId);
				}

				// Set the extraction dataset id
				ip
					.getInterventions()
					.forEach(intervention -> {
						intervention.setExtractionDatasetId(props.datasetId);
						intervention.setId(UUID.randomUUID());
					});

				interventionService.createAsset(ip, props.projectId);
			}
		} catch (final Exception e) {
			log.error("Failed to extract intervention policy", e);
			throw new RuntimeException(e);
		}
		log.info("Intervention policy extracted successfully");
		return resp;
	}
}
