package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigureModelFromDatasetResponseHandler extends LlmTaskResponseHandler {

	public static final String NAME = "gollm:configure_model_from_dataset";

	private final ObjectMapper objectMapper;
	private final ModelConfigurationService modelConfigurationService;
	private final DatasetService datasetService;

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

		@JsonProperty("matrix")
		String matrix;
	}

	@Data
	public static class Response {

		JsonNode response;
	}

	@Data
	public static class Properties {

		UUID datasetId;
		UUID projectId;
		UUID modelId;
		UUID workflowId;
		UUID nodeId;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final Response configurations = objectMapper.readValue(resp.getOutput(), Response.class);

			// For each configuration, create a new model configuration
			for (final JsonNode condition : configurations.response.get("conditions")) {
				final ModelConfiguration configuration = objectMapper.treeToValue(condition, ModelConfiguration.class);

				if (configuration.getModelId() != props.modelId) {
					configuration.setModelId(props.modelId);
				}

				if (configuration.getExtractionDocumentId() != props.datasetId) {
					configuration.setExtractionDocumentId(props.datasetId);
				}

				// Fetch the dataset name
				final Optional<Dataset> dataset = datasetService.getAsset(props.datasetId);
				final String source = dataset.map(TerariumAsset::getName).orElse(null);

				// Update the source of the model-configuration with the Dataset name
				if (source != null) {
					configuration.getInitialSemanticList().forEach(initial -> initial.setSource(source));
					configuration.getParameterSemanticList().forEach(parameter -> parameter.setSource(source));
				}

				modelConfigurationService.createAsset(configuration, props.projectId);
			}
		} catch (final Exception e) {
			log.error("Failed to configure model", e);
			throw new RuntimeException(e);
		}
		log.info("Model configured successfully");
		return resp;
	}
}
