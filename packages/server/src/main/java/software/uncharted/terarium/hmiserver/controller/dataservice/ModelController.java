package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProvenanceProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;

@RequestMapping("/models")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ModelController {

	final ModelService modelService;

	final DocumentAssetService documentAssetService;

	// final ModelProxy modelProxy;

	final ProvenanceProxy provenanceProxy;

	final ObjectMapper objectMapper;

	@GetMapping("/descriptions")
	@Secured(Roles.USER)
	public ResponseEntity<List<ModelDescription>> listModels(
			@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) throws IOException {
		return ResponseEntity.ok(modelService.getDescriptions(page, pageSize));
	}

	@GetMapping("/{id}/descriptions")
	@Secured(Roles.USER)
	ResponseEntity<ModelDescription> getDescription(
			@PathVariable("id") String id) throws IOException {
		return ResponseEntity.ok(modelService.getDescription(id));
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	ResponseEntity<Model> getModel(@PathVariable("id") String id) throws IOException {
		Model model;
		final ObjectMapper mapper = new ObjectMapper();

		// Fetch the model from the data-service
		try {
			model = modelService.getModel(id);
		} catch (RuntimeException e) {
			log.error("Unable to get the model" + id, e);
			return ResponseEntity.internalServerError().build();
		}

		if (model == null) {
			return ResponseEntity.noContent().build();
		}

		// Find the Document Assets linked via provenance to the model
		ProvenanceQueryParam body = new ProvenanceQueryParam();
		body.setRootId(id);
		body.setRootType(ProvenanceType.MODEL);
		body.setTypes(List.of(ProvenanceType.DOCUMENT));
		final JsonNode provenanceResults = provenanceProxy.search(body, "models_from_document").getBody();
		if (provenanceResults != null) {
			final JsonNode resultsNode = provenanceResults.get("result");

			// If there are results, fetch the Documents
			if (resultsNode != null && resultsNode.isArray() && !resultsNode.isEmpty()) {
				// Get the list as Document Ids
				final List<String> documentIds = mapper.convertValue(resultsNode, new TypeReference<List<String>>() {
				});

				// Make sure we have an attributes list
				if (model.getMetadata().getAttributes() == null)
					model.getMetadata().setAttributes(new ArrayList<>());

				documentIds.forEach(documentId -> {
					try {
						// Fetch the Document extractions
						final DocumentAsset document = documentAssetService.getDocumentAsset(documentId);
						if (document != null) {
							final List<JsonNode> extractions = mapper.convertValue(
									document.getMetadata().get("attributes"), new TypeReference<List<JsonNode>>() {
									});

							// Append the Document extractions to the Model extractions, just for the
							// front-end.
							// Those are NOT to be saved back to the data-service.
							model.getMetadata().getAttributes().addAll(extractions);
						}
					} catch (Exception e) {
						log.error("Unable to get the document " + documentId, e);
					}
				});
			}
		} else {
			log.debug("Unable to get the, or empty, provenance search models_from_document for model " + id);
		}

		// Return the model
		return ResponseEntity.ok(model);
	}

	@GetMapping("/search")
	@Secured(Roles.USER)
	public ResponseEntity<List<Model>> searchModels(
			@RequestBody JsonNode query,
			@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) throws IOException {
		return ResponseEntity.ok(modelService.searchModels(page, pageSize, query));
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	ResponseEntity<ResponseId> updateModel(
			@PathVariable("id") String id,
			@RequestBody Model model) throws IOException {

		modelService.updateModel(model.setId(id));

		return ResponseEntity.ok(new ResponseId().setId(id));
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	ResponseEntity<ResponseDeleted> deleteModel(
			@PathVariable("id") String id) throws IOException {

		modelService.deleteModel(id);
		return ResponseEntity.ok(new ResponseDeleted("Model", id));
	}

	@PostMapping
	@Secured(Roles.USER)
	ResponseEntity<ResponseId> createModel(
			@RequestBody Model model) throws IOException {

		modelService.createModel(model);
		return ResponseEntity.ok(new ResponseId().setId(model.getId()));
	}

	@GetMapping("/{id}/model_configurations")
	@Secured(Roles.USER)
	ResponseEntity<List<ModelConfiguration>> getModelConfigurationsForModelId(
			@PathVariable("id") String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "page_size", required = false, defaultValue = "100") int pageSize)
			throws IOException {
		return ResponseEntity.ok(modelService.getModelConfigurationsByModelId(id, page, pageSize));
	}
}
