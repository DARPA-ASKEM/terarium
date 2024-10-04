package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateModelConfigHandler extends TaskResponseHandler {

	public static final String NAME = "funman_task:validate_modelconfig";

	private final ObjectMapper objectMapper;
	private final SimulationService simulationService;
	private final ModelService modelService;
	private final ModelConfigurationService modelConfigurationService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Properties {

		UUID projectId;
		UUID modelId;
		UUID simulationId;
	}

	@Override
	public TaskResponse onRunning(final TaskResponse resp) {
		// FIXME: remove when we distinguish between "initialized" vs "running" state
		if (resp.getOutput() == null) {
			return resp;
		}

		try {
			final JsonNode intermediateResult = objectMapper.readValue(resp.getOutput(), JsonNode.class);
			final double progress = intermediateResult.get("progress").doubleValue();

			final Properties props = resp.getAdditionalProperties(Properties.class);
			final UUID simulationId = props.getSimulationId();
			final Optional<Simulation> sim = simulationService.getAsset(
				simulationId,
				ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER
			);
			if (!sim.isEmpty()) {
				sim.get().setProgress(progress);
				simulationService.updateAsset(sim.get(), props.projectId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		return resp;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		final String resultFilename = "validation.json";
		try {
			// Parse validation result
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final UUID simulationId = props.getSimulationId();
			final Optional<Simulation> sim = simulationService.getAsset(
				simulationId,
				ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER
			);
			if (sim.isEmpty()) {
				log.error("Cannot find Simulation " + simulationId + " for task " + resp.getId());
				throw new Error("Cannot find Simulation " + simulationId + " for task " + resp.getId());
			}

			// Retrive final result json
			final JsonNode result = objectMapper.readValue(resp.getOutput(), JsonNode.class);

			// Upload final result into S3
			final byte[] bytes = objectMapper.writeValueAsBytes(result.get("response"));
			final HttpEntity fileEntity = new ByteArrayEntity(bytes, ContentType.TEXT_PLAIN);
			simulationService.uploadFile(simulationId, resultFilename, fileEntity);

			// Save contracted model/model configuration
			try {
				// The response is stringified JSON so convert it to an object to access the contracted_model and clean it up
				String responseString = result.get("response").asText();
				ObjectNode contractedModelObject = (ObjectNode) objectMapper.readTree(responseString).get("contracted_model");
				if (contractedModelObject != null) {
					// Rename schema_ to schema. Currently it has an extra underscore, remove this once funman fixes this
					ObjectNode headerObject = (ObjectNode) contractedModelObject.get("header");
					if (headerObject != null && headerObject.has("schema_")) {
						JsonNode schemaNode = headerObject.remove("schema_");
						headerObject.set("schema", schemaNode);
					}
					final Model contractedModel = objectMapper.convertValue(contractedModelObject, Model.class);
					final ModelConfiguration contractedModelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(
						contractedModel,
						"Validated " + contractedModel.getName(),
						contractedModel.getDescription()
					);
					contractedModelConfiguration.setModelId(props.modelId);
					System.out.println("contractedModelConfiguration||||||||||||||: " + contractedModelConfiguration);
					// JsonNode outputNode = objectMapper.readTree(resp.getOutput());

					//   // Step 2: Add the new property
					//   if (outputNode.isObject()) {
					//       ((ObjectNode) outputNode).put("modelConfigId", createdModel.getId().toString());
					//   }

					//   // Step 3: Serialize the updated JSON
					//   byte[] updatedOutput = objectMapper.writeValueAsBytes(outputNode);

					//   // Step 4: Set the updated output
					//   resp.setOutput(updatedOutput);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Mark simulation as completed, update result file
			sim.get().setStatus(ProgressState.COMPLETE);
			final ArrayList<String> resultFiles = new ArrayList<String>();
			resultFiles.add(resultFilename);
			sim.get().setResultFiles(resultFiles);

			// Save
			simulationService.updateAsset(sim.get(), props.projectId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return resp;
	}
}
