package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Initial;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;
import software.uncharted.terarium.hmiserver.service.gollm.ScenarioExtraction;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigureModelResponseHandler extends TaskResponseHandler {
	public static final String NAME = "gollm_task:configure_model";

	private final ObjectMapper objectMapper;
	private final ModelService modelService;
	private final ModelConfigurationService modelConfigurationService;
	private final ProvenanceService provenanceService;

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
		UUID projectId;
		UUID documentId;
		UUID modelId;
		UUID workflowId;
		UUID nodeId;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final Model model = modelService
					.getAsset(props.getModelId(), ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER)
					.orElseThrow();
			final Response configurations = objectMapper.readValue(resp.getOutput(), Response.class);

			// For each configuration, create a new model configuration with parameters set
			for (final JsonNode condition : configurations.response.get("conditions")) {
				final Model modelCopy = model.clone();
				modelCopy.setId(model.getId());

				// Map the parameters values to the model
				final ArrayNode gollmExtractions = objectMapper.createArrayNode();
				if (condition.has("parameters")) {
					final List<ModelParameter> modelParameters = ScenarioExtraction
							.getModelParameters(condition.get("parameters"), modelCopy);
					if (modelCopy.isRegnet()) {
						modelCopy
								.getModel()
								.put("parameters", objectMapper.convertValue(modelParameters, JsonNode.class));
					}
					final ArrayNode parameters = condition.get("parameters").deepCopy();
					gollmExtractions.addAll(parameters);
				}

				// Map the initials values to the model
				// final ArrayNode gollmExtractionsInitials = objectMapper.createArrayNode();
				if (condition.has("initials")) {
					final List<Initial> modelInitials = ScenarioExtraction.getModelInitials(condition.get("initials"),
							modelCopy);
					if (modelCopy.isRegnet()) {
						modelCopy.getModel().put("initials", objectMapper.convertValue(modelInitials, JsonNode.class));
					}
					final ArrayNode initials = condition.get("initials").deepCopy();
					gollmExtractions.addAll(initials);
				}

				// Set the all the GoLLM extractions into the model metadata
				// FIXME - It is not what we should do, this is a hack for the March 2024
				// Evaluation
				if (model.getMetadata() == null) {
					model.setMetadata(new ModelMetadata());
				}
				model.getMetadata().setGollmExtractions(gollmExtractions);

				// Create the new configuration
				final ModelConfiguration configuration = ModelConfigurationService.modelConfigurationFromAMR(
						model,
						condition.get("name").asText(),
						condition.get("description").asText());

				final ModelConfiguration newConfig = modelConfigurationService.createAsset(
						configuration, props.projectId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
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
