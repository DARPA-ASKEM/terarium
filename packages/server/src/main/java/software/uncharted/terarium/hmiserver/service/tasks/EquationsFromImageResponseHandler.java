package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractedDocumentPage;
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

	public static final String NAME = "gollm:equations_from_image";

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

			// We can get with or without a document id, if there is one, attach the new equations to the document
			if (props.documentId != null) {
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
				existingEquations.add(equations.response.get("equations"));
				document.getMetadata().put("equations", existingEquations);

				// add to the extractions field
				final List<JsonNode> newEquations = new ArrayList<>();
				for (final JsonNode node : equations.response.get("equations")) {
					newEquations.add(node);
				}
				final ExtractedDocumentPage newPage = new ExtractedDocumentPage()
					.setPageNumber(document.getExtractions().size() + 1)
					.setEquations(newEquations);
				document.getExtractions().add(newPage);

				System.out.println("Document: " + document);
				//documentService.updateAsset(document, props.projectId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER).orElseThrow();

				// add provenance
				provenanceService.createProvenance(
					new Provenance()
						.setLeft(props.getDocumentId())
						.setLeftType(ProvenanceType.EQUATION)
						.setRight(props.getDocumentId())
						.setRightType(ProvenanceType.DOCUMENT)
						.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
				);
			}
		} catch (final Exception e) {
			log.error("Failed to configure model", e);
			throw new RuntimeException(e);
		}
		log.info("Model configured successfully");
		return resp;
	}
}
