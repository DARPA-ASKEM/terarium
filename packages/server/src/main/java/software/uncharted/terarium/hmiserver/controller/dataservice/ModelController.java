package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;
import software.uncharted.terarium.hmiserver.repository.data.InterventionRepository;
import software.uncharted.terarium.hmiserver.repository.data.ModelConfigRepository;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService.ModelConfigurationUpdate;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/models")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ModelController {

	final InterventionRepository interventionRepository;
	final ModelConfigRepository modelConfigRepository;
	final ModelConfigurationService modelConfigurationService;
	final ModelService modelService;
	final ProjectAssetService projectAssetService;
	final ProjectService projectService;

	@GetMapping("/{id}/descriptions")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model description by ID")
	@HasProjectAccess
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model description found.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelDescription.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "There was no description found", content = @Content),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the description from the data store",
				content = @Content
			)
		}
	)
	ResponseEntity<ModelDescription> getDescription(
		@PathVariable("id") final UUID id,
		@RequestParam("project-id") final UUID projectId
	) {
		try {
			final Optional<ModelDescription> model = modelService.getDescription(id);
			return model.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to get model description";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model by ID")
	@HasProjectAccess
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model found.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the model from the data store",
				content = @Content
			)
		}
	)
	ResponseEntity<Model> getModel(
		@PathVariable("id") final UUID id,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
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

			// Force proper annotation metadata
			final ModelMetadata metadata = model.get().getMetadata();
			if (metadata.getAnnotations() == null) {
				metadata.setAnnotations(new Annotations());
				model.get().setMetadata(metadata);
			}

			// Return the model
			return ResponseEntity.ok(model.get());
		} catch (final Exception e) {
			final String error = "Unable to get model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a model")
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model updated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "Model could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the model", content = @Content)
		}
	)
	ResponseEntity<Model> updateModel(
		@PathVariable("id") final UUID id,
		@RequestBody final Model model,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			final Optional<Model> originalModel = modelService.getAsset(id);
			if (originalModel.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			model.setId(id);
			// Set the model name from the AMR header name.
			// TerariumAsset have a name field, but it's not used for the model name outside
			// the front-end.
			final Optional<Model> updated = modelService.updateAsset(model, projectId);

			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to update model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an model")
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Deleted model",
				content = {
					@Content(
						mediaType = "application/json",
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)
					)
				}
			),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
		}
	)
	ResponseEntity<ResponseDeleted> deleteModel(
		@PathVariable("id") final UUID id,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			modelService.deleteAsset(id, projectId);
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
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Model created.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the model", content = @Content)
		}
	)
	ResponseEntity<Model> createModel(
		@RequestBody final Model model,
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestParam(name = "model-configuration-id", required = false) final UUID modelConfigId
	) {
		try {
			// Set the model name from the AMR header name.
			// TerariumAsset have a name field, but it's not used for the model name outside
			// the front-end.

			ModelConfiguration oldModelConfiguration = null;
			if (modelConfigId != null) {
				oldModelConfiguration = modelConfigurationService.getAsset(modelConfigId).get();
			}

			final ModelConfigurationUpdate options = new ModelConfigurationUpdate();
			if (oldModelConfiguration != null) {
				options.setTemporalContext(oldModelConfiguration.getTemporalContext());
			}

			model.setName(model.getHeader().getName());
			final Model created = modelService.createAsset(model, projectId);

			// create default configuration
			final ModelConfiguration modelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(
				created,
				options
			);
			modelConfigurationService.createAsset(modelConfiguration, projectId);

			// add default model configuration to project
			final Optional<Project> project = projectService.getProject(projectId);
			project.ifPresent(value ->
				projectAssetService.createProjectAsset(value, AssetType.MODEL_CONFIGURATION, modelConfiguration)
			);

			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (final IOException e) {
			final String error = "Unable to create model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@Data
	public static class CreateModelFromOldRequest {

		Model oldModel;
		Model newModel;
	}

	@PostMapping("/new-from-old")
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model from an old model")
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Model created.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Model.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the model", content = @Content)
		}
	)
	ResponseEntity<Model> createModelFromOld(
		@RequestBody final CreateModelFromOldRequest req,
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestParam(name = "model-configuration-id", required = false) final UUID modelConfigId
	) {
		try {
			req.newModel.retainMetadataFields(req.oldModel);

			// Set the model name from the AMR header name.
			// TerariumAsset have a name field, but it's not used for the model name outside
			// the front-end.
			req.newModel.setName(req.newModel.getHeader().getName());
			final Model created = modelService.createAsset(req.newModel, projectId);

			ModelConfiguration oldModelConfiguration = null;
			if (modelConfigId != null) {
				oldModelConfiguration = modelConfigurationService.getAsset(modelConfigId).get();
			}

			final ModelConfigurationUpdate options = new ModelConfigurationUpdate();
			if (oldModelConfiguration != null) {
				options.setTemporalContext(oldModelConfiguration.getTemporalContext());
			}

			// create default configuration
			final ModelConfiguration modelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(
				created,
				options
			);
			modelConfigurationService.createAsset(modelConfiguration, projectId);

			// add default model configuration to project
			final Optional<Project> project = projectService.getProject(projectId);
			project.ifPresent(value ->
				projectAssetService.createProjectAsset(value, AssetType.MODEL_CONFIGURATION, modelConfiguration)
			);

			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (final IOException e) {
			final String error = "Unable to create model";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/model-configurations")
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model configurations for a model")
	@HasProjectAccess
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model configurations found",
				content = @Content(
					array = @ArraySchema(
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelConfiguration.class)
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving configurations from the data store",
				content = @Content
			)
		}
	)
	ResponseEntity<List<ModelConfiguration>> getModelConfigurationsForModelId(
		@PathVariable("id") final UUID id,
		@RequestParam(value = "page", required = false, defaultValue = "0") final int page,
		@RequestParam(value = "page-size", required = false, defaultValue = "100") final int pageSize,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			final List<ModelConfiguration> modelConfigurations =
				modelConfigRepository.findByModelIdAndDeletedOnIsNullAndTemporaryFalseOrderByCreatedOnAsc(
					id,
					PageRequest.of(page, pageSize)
				);

			return ResponseEntity.ok(modelConfigurations);
		} catch (final Exception e) {
			final String error = "Unable to get model configurations";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/intervention-policies")
	@Secured(Roles.USER)
	@Operation(summary = "Gets all intervention policies for a model")
	@HasProjectAccess
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Interventions policies found.",
				content = @Content(
					array = @ArraySchema(
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = InterventionPolicy.class)
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving policies from the data store",
				content = @Content
			)
		}
	)
	ResponseEntity<List<InterventionPolicy>> getInterventionsForModelId(
		@PathVariable("id") final UUID id,
		@RequestParam(value = "page", required = false, defaultValue = "0") final int page,
		@RequestParam(value = "page-size", required = false, defaultValue = "100") final int pageSize,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			final List<InterventionPolicy> interventionPolicies =
				interventionRepository.findByModelIdAndDeletedOnIsNullAndTemporaryFalse(id, PageRequest.of(page, pageSize));

			return ResponseEntity.ok(interventionPolicies);
		} catch (final Exception e) {
			final String error = "Unable to get intervention policies";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PostMapping("/amr-to-model-configuration")
	@Secured(Roles.USER)
	@Operation(summary = "Formats a model configuration from an AMR")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model configurations found.",
				content = @Content(
					array = @ArraySchema(
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelConfiguration.class)
					)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving configurations from the data store",
				content = @Content
			)
		}
	)
	static ResponseEntity<ModelConfiguration> modelConfigurationFromAmr(
		@RequestBody final Model model,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			final ModelConfigurationUpdate options = new ModelConfigurationUpdate();
			options.setName(model.getName());
			options.setName(model.getDescription());
			final ModelConfiguration modelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(model, options);
			return ResponseEntity.ok(modelConfiguration);
		} catch (final Exception e) {
			final String error = "Unable to get model configurations";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
