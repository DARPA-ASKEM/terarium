package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceSearchService;

@RequestMapping("/models")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ModelController {

	final ModelService modelService;

	final DocumentAssetService documentAssetService;

	final ProvenanceSearchService provenanceSearchService;

	final ObjectMapper objectMapper;

	@GetMapping("/descriptions")
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model descriptions")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model descriptions found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelDescription.class)))),
			@ApiResponse(responseCode = "204", description = "There are no descriptions found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving descriptions from the data store", content = @Content)
	})
	public ResponseEntity<List<ModelDescription>> listModels(
			@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {

		try {
			return ResponseEntity.ok(modelService.getDescriptions(page, pageSize));
		} catch (IOException e) {
			final String error = "Unable to get model";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}/descriptions")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model description by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model description found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelDescription.class))),
			@ApiResponse(responseCode = "204", description = "There was no description found but no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the description from the data store", content = @Content)
	})
	ResponseEntity<ModelDescription> getDescription(
			@PathVariable("id") UUID id) {

		try {
			return ResponseEntity.ok(modelService.getDescription(id));
		} catch (IOException e) {
			final String error = "Unable to get model description";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class))),
			@ApiResponse(responseCode = "204", description = "There was no model found but no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the model from the data store", content = @Content)
	})
	ResponseEntity<Model> getModel(@PathVariable("id") UUID id) {

		try {

			// Fetch the model from the data-service
			Model model = modelService.getModel(id);
			if (model == null) {
				return ResponseEntity.noContent().build();
			}

			// Find the Document Assets linked via provenance to the model
			ProvenanceQueryParam body = new ProvenanceQueryParam();
			body.setRootId(id);
			body.setRootType(ProvenanceType.MODEL);
			body.setTypes(List.of(ProvenanceType.DOCUMENT));
			final Set<String> documentIds = provenanceSearchService.modelsFromDocument(body);
			if (!documentIds.isEmpty()) {
				// Make sure we have an attributes list
				if (model.getMetadata().getAttributes() == null)
					model.getMetadata().setAttributes(new ArrayList<>());

				documentIds.forEach(documentId -> {
					try {
						// Fetch the Document extractions
						final DocumentAsset document = documentAssetService
								.getDocumentAsset(UUID.fromString(documentId));
						if (document != null) {
							final List<JsonNode> extractions = objectMapper.convertValue(
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
			} else {
				log.debug("Unable to get the, or empty, provenance search models_from_document for model " + id);
			}

			// Return the model
			return ResponseEntity.ok(model);
		} catch (IOException e) {
			final String error = "Unable to get model";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/search")
	@Secured(Roles.USER)
	@Operation(summary = "Search models with a query")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Models found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class)))),
			@ApiResponse(responseCode = "204", description = "There are no models found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving models from the data store", content = @Content)
	})
	public ResponseEntity<List<Model>> searchModels(
			@RequestBody JsonNode query,
			@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {

		try {
			return ResponseEntity.ok(modelService.searchModels(page, pageSize, query));
		} catch (IOException e) {
			final String error = "Unable to search models";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a model")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the model", content = @Content)
	})
	ResponseEntity<Model> updateModel(
			@PathVariable("id") UUID id,
			@RequestBody Model model) {

		try {
			final Optional<Model> updated = modelService.updateModel(model.setId(id));
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (IOException e) {
			final String error = "Unable to update model";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an model")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted model", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "Model could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	ResponseEntity<ResponseDeleted> deleteModel(
			@PathVariable("id") UUID id) {

		try {
			modelService.deleteModel(id);
			return ResponseEntity.ok(new ResponseDeleted("Model", id));
		} catch (IOException e) {
			final String error = "Unable to delete model";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the model", content = @Content)
	})
	ResponseEntity<Model> createModel(
			@RequestBody Model model) {

		try {

			model = modelService.createModel(model);
			return ResponseEntity.ok(model);
		} catch (IOException e) {
			final String error = "Unable to create model";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}/model_configurations")
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model configurations for a model")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model configurations found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelConfiguration.class)))),
			@ApiResponse(responseCode = "204", description = "There are no configurations found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving configurations from the data store", content = @Content)
	})
	ResponseEntity<List<ModelConfiguration>> getModelConfigurationsForModelId(
			@PathVariable("id") UUID id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "page_size", required = false, defaultValue = "100") int pageSize) {

		try {
			return ResponseEntity.ok(modelService.getModelConfigurationsByModelId(id, page, pageSize));
		} catch (IOException e) {
			final String error = "Unable to get model configurations";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}
}
