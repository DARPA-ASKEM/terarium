package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.InterventionService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterventionsFromDocumentResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm:interventions_from_document";

	private final ObjectMapper objectMapper;
	private final InterventionService interventionService;

	@SuppressWarnings("unused")
	private final ProvenanceService provenanceService;

	@SuppressWarnings("unused")
	private final DocumentAssetService documentAssetService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		@JsonProperty("research_paper")
		String researchPaper;

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
		UUID documentId;
		UUID modelId;
		UUID workflowId;
		UUID nodeId;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final Response interventionPolicies = objectMapper.readValue(resp.getOutput(), Response.class);

			// For each configuration, create a new model configuration
			for (final JsonNode policy : interventionPolicies.response.get("interventionPolicies")) {
				final InterventionPolicy ip = objectMapper.treeToValue(policy, InterventionPolicy.class);

				if (ip.getModelId() != props.modelId) {
					ip.setModelId(props.modelId);
				}

				// Set the extraction document id
				ip.getInterventions().forEach(intervention -> intervention.setExtractionDocumentId(props.documentId));

				@SuppressWarnings("unused")
				final InterventionPolicy newPolicy = interventionService.createAsset(
					ip,
					props.projectId,
					ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER
				);
			}
		} catch (final Exception e) {
			log.error("Failed to extract intervention policy", e);
			throw new RuntimeException(e);
		}
		log.info("Intervention policy extracted successfully");
		return resp;
	}
}
