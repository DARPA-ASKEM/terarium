package software.uncharted.terarium.hmiserver.controller.dataservice;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
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

import java.io.IOException;
import java.util.*;

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
			@RequestParam(name = "page-size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {

		try {
			return ResponseEntity.ok(modelService.getDescriptions(page, pageSize));
		} catch (final IOException e) {
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
			@ApiResponse(responseCode = "204", description = "There was no description found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the description from the data store", content = @Content)
	})
	ResponseEntity<ModelDescription> getDescription(
			@PathVariable("id") final UUID id) {

		try {
			final Optional<ModelDescription> model = modelService.getDescription(id);
			return model.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
		} catch (final IOException e) {
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
			@ApiResponse(responseCode = "204", description = "There was no model found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the model from the data store", content = @Content)
	})
	ResponseEntity<Model> getModel(@PathVariable("id") final UUID id) {

		try {

			// Fetch the model from the data-service
			final Optional<Model> model = modelService.getAsset(id);
			if (model.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			// Find the Document Assets linked via provenance to the model
			final ProvenanceQueryParam body = new ProvenanceQueryParam();
			body.setRootId(id);
			body.setRootType(ProvenanceType.MODEL);
			body.setTypes(List.of(ProvenanceType.DOCUMENT));
			final Set<String> documentIds = provenanceSearchService.modelsFromDocument(body);
			if (!documentIds.isEmpty()) {
				// Make sure we have an attributes list
				if (model.get().getMetadata().getAttributes() == null)
					model.get().getMetadata().setAttributes(new ArrayList<>());

				documentIds.forEach(documentId -> {
					try {
						// Fetch the Document extractions
						final Optional<DocumentAsset> document = documentAssetService
								.getAsset(UUID.fromString(documentId));
						if (document.isPresent()) {
							final List<JsonNode> extractions = objectMapper.convertValue(
								document.get().getMetadata().get("attributes"), new TypeReference<>() {});

							// Append the Document extractions to the Model extractions, just for the
							// front-end.
							// Those are NOT to be saved back to the data-service.
							model.get().getMetadata().getAttributes().addAll(extractions);
						}
					} catch (final Exception e) {
						log.error("Unable to get the document " + documentId, e);
					}
				});
			} else {
				log.debug("Unable to get the, or empty, provenance search models_from_document for model " + id);
			}

			// Return the model
			return ResponseEntity.ok(model.get());
		} catch (final IOException e) {
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
			@RequestBody final JsonNode query,
			@RequestParam(name = "page-size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {

		try {
			return ResponseEntity.ok(modelService.searchModels(page, pageSize, query));
		} catch (final IOException e) {
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
			@ApiResponse(responseCode = "200", description = "Model updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class))),
			@ApiResponse(responseCode = "404", description = "Model could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the model", content = @Content)
	})
	ResponseEntity<Model> updateModel(
			@PathVariable("id") final UUID id,
			@RequestBody final Model model) {

		try {
			model.setId(id);
			final Optional<Model> updated = modelService.updateAsset(model);
			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
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
			@PathVariable("id") final UUID id) {

		try {
			modelService.deleteAsset(id);
			return ResponseEntity.ok(new ResponseDeleted("Model", id));
		} catch (final IOException e) {
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
			@ApiResponse(responseCode = "201", description = "Model created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the model", content = @Content)
	})
	ResponseEntity<Model> createModel(
			@RequestBody Model model) {

		try {
			model = modelService.createAsset(model);
			return ResponseEntity.status(HttpStatus.CREATED).body(model);
		} catch (final IOException e) {
			final String error = "Unable to create model";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}/model-configurations")
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model configurations for a model")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model configurations found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelConfiguration.class)))),
			@ApiResponse(responseCode = "204", description = "There are no configurations found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving configurations from the data store", content = @Content)
	})
	ResponseEntity<List<ModelConfiguration>> getModelConfigurationsForModelId(
			@PathVariable("id") final UUID id,
			@RequestParam(value = "page", required = false, defaultValue = "0") final int page,
			@RequestParam(value = "page-size", required = false, defaultValue = "100") final int pageSize) {

		try {
			return ResponseEntity.ok(modelService.getModelConfigurationsByModelId(id, page, pageSize));
		} catch (final IOException e) {
			final String error = "Unable to get model configurations";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}
}
