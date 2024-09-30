package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
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
	private final ProvenanceService provenanceService;
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

				final InterventionPolicy newPolicy = interventionService.createAsset(
					ip,
					props.projectId,
					ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER
				);

				// add provenance
				provenanceService.createProvenance(
					new Provenance()
						.setLeft(newPolicy.getId())
						.setLeftType(ProvenanceType.INTERVENTION_POLICY)
						.setRight(props.documentId)
						.setRightType(ProvenanceType.DOCUMENT)
						.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
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
