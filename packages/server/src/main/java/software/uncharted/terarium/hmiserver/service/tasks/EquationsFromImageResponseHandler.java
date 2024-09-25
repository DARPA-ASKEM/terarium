package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

@Component
@RequiredArgsConstructor
@Slf4j
public class EquationsFromImageResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm_task:equations_from_image";

	private final ObjectMapper objectMapper;
	private final DocumentAssetService documentService;
	private final ProvenanceService provenanceService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		// base64 encoded image
		String image;
	}

	@Data
	public static class Properties {

		UUID projectId;
		UUID documentId;
	}

	@Data
	public static class Response {

		JsonNode response;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final EquationsFromImageResponseHandler.Properties props = resp.getAdditionalProperties(
				EquationsFromImageResponseHandler.Properties.class
			);
			final EquationsFromImageResponseHandler.Response equations = objectMapper.readValue(
				resp.getOutput(),
				EquationsFromImageResponseHandler.Response.class
			);

			final Optional<DocumentAsset> documentAsset = documentService.getAsset(
				props.documentId,
				ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER
			);

			if (documentAsset.isEmpty()) {
				throw new IllegalArgumentException("Document not found: " + props.documentId);
			}

			final DocumentAsset document = documentAsset.get();
			if (document.getMetadata() == null) {
				document.setMetadata(new HashMap<>());
			}

			if (!document.getMetadata().containsKey("equations")) {
				document.getMetadata().put("equations", objectMapper.valueToTree(new ArrayList<JsonNode>()));
			}

			// get the existing equations and add the new ones
			final ArrayNode existingEquations = (ArrayNode) document.getMetadata().get("equations");
			for (final JsonNode equation : equations.response.get("equations")) {
				existingEquations.add(equation);
			}

			document.getMetadata().put("equations", existingEquations);

			documentService.updateAsset(document, props.projectId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER).orElseThrow();

			// add provenance
			provenanceService.createProvenance(
				new Provenance()
					.setLeft(props.getDocumentId())
					.setLeftType(ProvenanceType.EQUATION)
					.setRight(props.getDocumentId())
					.setRightType(ProvenanceType.DOCUMENT)
					.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
			);
		} catch (final Exception e) {
			log.error("Failed to configure model", e);
			throw new RuntimeException(e);
		}
		log.info("Model configured successfully");
		return resp;
	}
}
