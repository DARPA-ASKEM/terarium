package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Initial;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.utils.GreekDictionary;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigureModelResponseHandler extends TaskResponseHandler {
	final public static String NAME = "gollm:configure_model";

	final private ObjectMapper objectMapper;
	final private ModelService modelService;
	final private ModelConfigurationService modelConfigurationService;
	final private ProvenanceService provenanceService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {
		@JsonProperty("research_paper")
		String researchPaper;

		@JsonProperty("amr")
		Model amr;
	}

	@Data
	public static class Response {
		JsonNode response;
	}

	@Data
	public static class Properties {
		UUID documentId;
		UUID modelId;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final Model model = modelService.getAsset(props.getModelId()).orElseThrow();
			final Response configurations = objectMapper.readValue(((TaskResponse) resp).getOutput(), Response.class);

			// For each configuration, create a new model configuration with parameters set
			for (final JsonNode condition : configurations.response.get("conditions")) {
				final Model modelCopy = new Model(model);

				// Map the parameters values to the model
				if (condition.has("parameters")) {
					final List<ModelParameter> modelParameters = ScenarioExtraction.getModelParameters(condition.get("parameters"), modelCopy);
					if (modelCopy.isRegnet()) {
						modelCopy.getModel().put("parameters", objectMapper.convertValue(modelParameters, JsonNode.class));
					}
				}

				// Map the initials values to the model
				if (condition.has("initials")) {
					final List<Initial> modelInitials = ScenarioExtraction.getModelInitials(condition.get("initials"), modelCopy);
					if (modelCopy.isRegnet()) {
						modelCopy.getModel().put("initials", objectMapper.convertValue(modelInitials, JsonNode.class));
					}
				}

				// Create the new configuration
				final ModelConfiguration configuration = new ModelConfiguration();
				configuration.setModelId(model.getId());
				configuration.setName(condition.get("name").asText());
				configuration.setDescription(condition.get("description").asText());
				configuration.setConfiguration(modelCopy);

				final ModelConfiguration newConfig = modelConfigurationService.createAsset(configuration);
				// add provenance
				provenanceService.createProvenance(new Provenance()
						.setLeft(newConfig.getId())
						.setLeftType(ProvenanceType.MODEL_CONFIGURATION)
						.setRight(props.documentId)
						.setRightType(ProvenanceType.DOCUMENT)
						.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));
			}
		} catch (final Exception e) {
			log.error("Failed to configure model", e);
			throw new RuntimeException(e);
		}
		log.info("Model configured successfully");
		return resp;
	}
}
