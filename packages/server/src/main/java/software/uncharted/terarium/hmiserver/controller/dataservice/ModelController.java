package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceSearchService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/models")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ModelController {

	final ModelService modelService;

	final DocumentAssetService documentAssetService;

	final ProvenanceSearchService provenanceSearchService;

	final ObjectMapper objectMapper;

	final DatasetService datasetService;

	final ProjectService projectService;

	final CurrentUserService currentUserService;

	final ProjectAssetService projectAssetService;

	@GetMapping("/descriptions")
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model descriptions")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model descriptions found.",
						content =
								@Content(
										array =
												@ArraySchema(
														schema =
																@io.swagger.v3.oas.annotations.media.Schema(
																		implementation = ModelDescription.class)))),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving descriptions from the data store",
						content = @Content)
			})
	public ResponseEntity<List<ModelDescription>> listModels(
			@RequestParam(name = "page-size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {

		try {
			return ResponseEntity.ok(modelService.getDescriptions(page, pageSize));
		} catch (final IOException e) {
			final String error = "Unable to get model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/descriptions")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model description by ID")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model description found.",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = ModelDescription.class))),
				@ApiResponse(responseCode = "404", description = "There was no description found", content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving the description from the data store",
						content = @Content)
			})
	ResponseEntity<ModelDescription> getDescription(@PathVariable("id") final UUID id) {

		try {
			final Optional<ModelDescription> model = modelService.getDescription(id);
			return model.map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to get model description";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model by ID")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model found.",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = Model.class))),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving the model from the data store",
						content = @Content)
			})
	ResponseEntity<Model> getModel(@PathVariable("id") final UUID id, @RequestParam("project-id") final UUID projectId) {
		Schema.Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {

			// Fetch the model from the data-service
			final Optional<Model> model = modelService.getAsset(id, permission);
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
				// Make sure we have a metadata object
				if (model.get().getMetadata() == null) {
					model.get().setMetadata(new ModelMetadata());
				}
				// Make sure we have an attributes list
				if (model.get().getMetadata().getAttributes() == null) {
					model.get().getMetadata().setAttributes(new ArrayList<>());
				}

				documentIds.forEach(documentId -> {
					try {
						// Fetch the Document extractions
						final Optional<DocumentAsset> document =
								documentAssetService.getAsset(UUID.fromString(documentId), permission);
						if (document.isPresent()) {
							if (document.get().getMetadata() == null) {
								document.get().setMetadata(new HashMap<>());
							}
							final List<JsonNode> extractions = objectMapper.convertValue(
									document.get().getMetadata().get("attributes"), new TypeReference<>() {});

							// Append the Document extractions to the Model extractions, just for the
							// front-end.
							// Those are NOT to be saved back to the data-service.
							if (extractions != null) {
								model.get().getMetadata().getAttributes().addAll(extractions);
							} else {
								log.error(
										"No attributes added to Model as DocumentAsset ({}) has no attributes.",
										documentId);
							}
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
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/search")
	@Secured(Roles.USER)
	@Operation(summary = "Search models with a query")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Models found.",
						content =
								@Content(
										array =
												@ArraySchema(
														schema =
																@io.swagger.v3.oas.annotations.media.Schema(
																		implementation = Model.class)))),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving models from the data store",
						content = @Content)
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
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a model")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model updated.",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = Model.class))),
				@ApiResponse(responseCode = "404", description = "Model could not be found", content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue updating the model",
						content = @Content)
			})
	ResponseEntity<Model> updateModel(@PathVariable("id") final UUID id, @RequestBody final Model model, @RequestParam("project-id") final UUID projectId) {
		Schema.Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			model.setId(id);
			// Set the model name from the AMR header name.
			// TerariumAsset have a name field, but it's not used for the model name outside
			// the front-end.
			final Optional<Model> updated = modelService.updateAsset(model, permission);
			return updated.map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to update model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an model")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Deleted model",
						content = {
							@Content(
									mediaType = "application/json",
									schema =
											@io.swagger.v3.oas.annotations.media.Schema(
													implementation = ResponseDeleted.class))
						}),
				@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
			})
	ResponseEntity<ResponseDeleted> deleteModel(@PathVariable("id") final UUID id, @RequestParam("project-id") final UUID projectId) {
		Schema.Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			modelService.deleteAsset(id, permission);
			return ResponseEntity.ok(new ResponseDeleted("Model", id));
		} catch (final IOException e) {
			final String error = "Unable to delete model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "201",
						description = "Model created.",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = Model.class))),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue creating the model",
						content = @Content)
			})
	ResponseEntity<Model> createModel(@RequestBody Model model, @RequestParam("project-id") final UUID projectId) {
		Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			// Set the model name from the AMR header name.
			// TerariumAsset have a name field, but it's not used for the model name outside
			// the front-end.
			model.setName(model.getHeader().getName());
			model = modelService.createAsset(model, permission);
			return ResponseEntity.status(HttpStatus.CREATED).body(model);
		} catch (final IOException e) {
			final String error = "Unable to create model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/model-configurations")
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model configurations for a model")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model configurations found.",
						content =
								@Content(
										array =
												@ArraySchema(
														schema =
																@io.swagger.v3.oas.annotations.media.Schema(
																		implementation = ModelConfiguration.class)))),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving configurations from the data store",
						content = @Content)
			})
	ResponseEntity<List<ModelConfiguration>> getModelConfigurationsForModelId(
			@PathVariable("id") final UUID id,
			@RequestParam(value = "page", required = false, defaultValue = "0") final int page,
			@RequestParam(value = "page-size", required = false, defaultValue = "100") final int pageSize, @RequestParam("project-id") final UUID projectId) {
		Schema.Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			final List<ModelConfiguration> modelConfigurations =
					modelService.getModelConfigurationsByModelId(id, page, pageSize);

			modelConfigurations.forEach(config -> {
				final Model configuration = config.getConfiguration();

				// check if configuration has a metadata field, if it doesnt make it an empty
				// object
				if (configuration.getMetadata() == null) {
					configuration.setMetadata(new ModelMetadata());
				}

				// Find the Document Assets linked via provenance to the model configuration
				final ProvenanceQueryParam documentQueryParams = new ProvenanceQueryParam();
				documentQueryParams.setRootId(config.getId());
				documentQueryParams.setRootType(ProvenanceType.MODEL_CONFIGURATION);
				documentQueryParams.setTypes(List.of(ProvenanceType.DOCUMENT));
				final Set<String> documentIds = provenanceSearchService.modelConfigFromDocument(documentQueryParams);

				final List<String> documentSourceNames = new ArrayList<>();
				documentIds.forEach(documentId -> {
					try {
						// Fetch the Document extractions
						final Optional<DocumentAsset> document =
								documentAssetService.getAsset(UUID.fromString(documentId), permission);
						if (document.isPresent()) {
							final String name = document.get().getName();
							documentSourceNames.add(name);
						}
					} catch (final Exception e) {
						log.error("Unable to get the document " + documentId, e);
					}
				});

				// Find the Dataset Assets linked via provenance to the model configuration
				final ProvenanceQueryParam datasetQueryParams = new ProvenanceQueryParam();
				datasetQueryParams.setRootId(config.getId());
				datasetQueryParams.setRootType(ProvenanceType.MODEL_CONFIGURATION);
				datasetQueryParams.setTypes(List.of(ProvenanceType.DATASET));
				final Set<String> datasetIds = provenanceSearchService.modelConfigFromDataset(datasetQueryParams);

				final List<String> datasetSourceNames = new ArrayList<>();
				datasetIds.forEach(datasetId -> {
					try {
						// Fetch the Document extractions
						final Optional<Dataset> dataset =
								datasetService.getAsset(UUID.fromString(datasetId), permission);
						if (dataset.isPresent()) {
							final String name = dataset.get().getName();
							documentSourceNames.add(name);
						}
					} catch (final Exception e) {
						log.error("Unable to get the document " + datasetId, e);
					}
				});

				final List<String> sourceNames = new ArrayList<>();
				sourceNames.addAll(documentSourceNames);
				sourceNames.addAll(datasetSourceNames);

				configuration.getMetadata().setSource(objectMapper.valueToTree(sourceNames));

				config.setConfiguration(configuration);
			});

			return ResponseEntity.ok(modelConfigurations);
		} catch (final Exception e) {
			final String error = "Unable to get model configurations";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
