package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigureFromDatasetResponseHandler extends TaskResponseHandler {
	final public static String NAME = "gollm:dataset_configure";

	final private ObjectMapper objectMapper;
	final private ModelService modelService;
	final private ModelConfigurationService modelConfigurationService;
	final private ProvenanceService provenanceService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void onSuccess(final Consumer<TaskResponse> resp) {
		try {
			final Properties props = ((TaskResponse) resp)
				.getAdditionalProperties(Properties.class);

			final Model model = modelService.getAsset(props.getModelId())
				.orElseThrow();
			final Response configurations = objectMapper.readValue(((TaskResponse) resp).getOutput(),
				Response.class);

			// For each configuration, create a new model configuration with parameters set
			configurations.response.get("conditions").forEach((condition) -> {
				// Map the parameters values to the model
				final Model modelCopy = new Model(model);
				final List<ModelParameter> modelParameters = modelCopy.getSemantics().getOde().getParameters();
				modelParameters.forEach((parameter) -> {
					final JsonNode conditionParameters = condition.get("parameters");
					conditionParameters.forEach((conditionParameter) -> {
						if (parameter.getId().equals(conditionParameter.get("id").asText())) {
							parameter.setValue(conditionParameter.get("value").doubleValue());
						}
					});
				});

				// Create the new configuration
				final ModelConfiguration configuration = new ModelConfiguration();
				configuration.setModelId(model.getId());
				configuration.setName(condition.get("name").asText());
				configuration.setDescription(condition.get("description").asText());
				configuration.setConfiguration(modelCopy);

				try {
					for (final UUID datasetId : props.datasetIds) {
						final ModelConfiguration newConfig = modelConfigurationService.createAsset(configuration);
						// add provenance
						provenanceService.createProvenance(new Provenance()
							.setLeft(newConfig.getId())
							.setLeftType(ProvenanceType.MODEL_CONFIGURATION)
							.setRight(datasetId)
							.setRightType(ProvenanceType.DATASET)
							.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));
					}

				} catch (final IOException e) {
					log.error("Failed to set model configuration", e);
				}
			});

		} catch (final Exception e) {
			log.error("Failed to configure model", e);
			throw new RuntimeException(e);
		}
		log.info("Model configured successfully");
	}

	@Data
	public static class Input {
		@JsonProperty("datasets")
		List<String> datasets;
		@JsonProperty("amr")
		Model amr;
	}

	@Data
	public static class Response {
		JsonNode response;
	}

	@Data
	public static class Properties {
		List<UUID> datasetIds;
		UUID modelId;
	}
}
