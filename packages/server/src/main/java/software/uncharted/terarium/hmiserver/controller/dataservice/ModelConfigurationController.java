package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema.Permission;

@RequestMapping("/model-configurations")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ModelConfigurationController {

	final ModelConfigurationService modelConfigurationService;
	final ModelService modelService;
	final CurrentUserService currentUserService;
	final Messages messages;
	final ProjectService projectService;

	/**
	 * Gets all model configurations (which are visible to this user)
	 *
	 * @param pageSize how many entries per page
	 * @param page page number
	 * @return all model configurations visible to this user
	 */
	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model configurations (which are visible to this user)")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "model configurations found.",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = ModelConfiguration.class)))
			),
			@ApiResponse(
				responseCode = "204",
				description = "There are no errors, but also no model configurations for this user",
				content = @Content
			),
			@ApiResponse(
				responseCode = "503",
				description = "There was an issue communicating with back-end services",
				content = @Content
			)
		}
	)
	public ResponseEntity<List<ModelConfiguration>> getModelConfigurations(
		@RequestParam(name = "page-size", defaultValue = "500") final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "1") final Integer page
	) {
		try {
			final List<ModelConfiguration> modelConfigurations = modelConfigurationService.getPublicNotTemporaryAssets(
				pageSize,
				page
			);
			if (modelConfigurations.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			return ResponseEntity.ok(modelConfigurations);
		} catch (final Exception e) {
			log.error("Unable to get model configurations from postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}

	/**
	 * Gets a specific model configuration by id
	 *
	 * @param id UUID of the specific model configuration to fetch
	 * @return the requested model configuration
	 */
	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a specific model configuration by id")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "model configuration found.",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ModelConfiguration.class))
			),
			@ApiResponse(
				responseCode = "404",
				description = "There was no configuration found by this ID",
				content = @Content
			),
			@ApiResponse(
				responseCode = "403",
				description = "User does not have permissions to this model configuration",
				content = @Content
			),
			@ApiResponse(
				responseCode = "503",
				description = "There was an issue communicating with back-end services",
				content = @Content
			)
		}
	)
	public ResponseEntity<ModelConfiguration> getModelConfiguration(@PathVariable("id") final UUID id) {
		final UUID projectId = modelConfigurationService.getProjectIdForAsset(id);
		final Permission permission = projectService.checkPermissionCanRead(currentUserService.get().getId(), projectId);
		try {
			final Optional<ModelConfiguration> modelConfiguration = modelConfigurationService.getAsset(id, permission);
			if (modelConfiguration.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("modelconfig.not-found"));
			}

			return ResponseEntity.ok(modelConfiguration.get());
		} catch (final Exception e) {
			log.error("Unable to get model configuration from postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}

	/**
	 * Create a configured model from a model config
	 *
	 * @param id id of the model configuration
	 * @return configured model
	 */
	@GetMapping("/as-configured-model/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a specific model configuration by id")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Configured model created",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = Model.class))
			),
			@ApiResponse(
				responseCode = "404",
				description = "There was no model or model configuration found by this ID",
				content = @Content
			),
			@ApiResponse(
				responseCode = "403",
				description = "User does not have permissions to this model configuration",
				content = @Content
			),
			@ApiResponse(
				responseCode = "503",
				description = "There was an issue communicating with back-end services",
				content = @Content
			)
		}
	)
	public ResponseEntity<Model> getConfiguredModel(@PathVariable("id") final UUID id) {
		final UUID projectId = modelConfigurationService.getProjectIdForAsset(id);
		final Permission permission = projectService.checkPermissionCanRead(currentUserService.get().getId(), projectId);
		try {
			final Optional<ModelConfiguration> modelConfiguration = modelConfigurationService.getAsset(id, permission);
			if (modelConfiguration.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("modelconfig.not-found"));
			}
			final Optional<Model> model = modelService.getAsset(modelConfiguration.get().getModelId(), permission);
			if (model.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("model.not-found"));
			}
			final Model newModel = ModelConfigurationService.createAMRFromConfiguration(
				model.get(),
				modelConfiguration.get()
			);
			return ResponseEntity.ok(newModel);
		} catch (final Exception e) {
			log.error("Unable to get model configuration from postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}

	@PostMapping("/as-configured-model/")
	@Secured(Roles.USER)
	@Operation(summary = "Creates a new model configuration based on a configured model")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Model configuration created from model.",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ModelConfiguration.class))
			),
			@ApiResponse(
				responseCode = "503",
				description = "There was an issue creating the configuration",
				content = @Content
			)
		}
	)
	public ResponseEntity<ModelConfiguration> createFromConfiguredModel(
		@RequestBody final Model configuredModel,
		@RequestParam(name = "name", required = false) final String name,
		@RequestParam(name = "description", required = false) final String description,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Permission permission = projectService.checkPermissionCanRead(currentUserService.get().getId(), projectId);

		final ModelConfiguration modelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(
			configuredModel,
			name,
			description
		);

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(
				modelConfigurationService.createAsset(modelConfiguration.clone(), projectId, permission)
			);
		} catch (final IOException e) {
			log.error("Unable to get model configuration from postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}

	@PutMapping("/as-configured-model/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Creates a new model configuration based on a configured model")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Model configuration created from model.",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ModelConfiguration.class))
			),
			@ApiResponse(
				responseCode = "503",
				description = "There was an issue creating the configuration",
				content = @Content
			)
		}
	)
	public ResponseEntity<ModelConfiguration> updateFromConfiguredModel(
		@PathVariable("id") final UUID id,
		@RequestBody final Model configuredModel,
		@RequestParam(name = "name", required = false) final String name,
		@RequestParam(name = "description", required = false) final String description
	) {
		final UUID projectId = modelConfigurationService.getProjectIdForAsset(id);
		final Permission permission = projectService.checkPermissionCanRead(currentUserService.get().getId(), projectId);

		final ModelConfiguration modelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(
			configuredModel,
			name,
			description
		);

		modelConfiguration.setId(id);

		try {
			final Optional<ModelConfiguration> optionalModelConfiguration = modelConfigurationService.updateAsset(
				modelConfiguration,
				projectId,
				permission
			);
			if (optionalModelConfiguration.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("modelconfig.not-found"));
			}
			return ResponseEntity.status(HttpStatus.CREATED).body(optionalModelConfiguration.get());
		} catch (final IOException e) {
			log.error("Unable to get model configuration from postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}

	/**
	 * Creates a new model config and saves it to the DB
	 *
	 * @param modelConfiguration new model config to create
	 * @param projectId owning project ID, used for permissions
	 * @return newly created model config with id set.
	 */
	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model configuration")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Model configuration created.",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ModelConfiguration.class))
			),
			@ApiResponse(
				responseCode = "503",
				description = "There was an issue creating the configuration",
				content = @Content
			)
		}
	)
	public ResponseEntity<ModelConfiguration> createModelConfiguration(
		@RequestBody final ModelConfiguration modelConfiguration,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			if (modelConfiguration.getId() == null) {
				modelConfiguration.setId(UUID.randomUUID());
			}

			return ResponseEntity.status(HttpStatus.CREATED).body(
				modelConfigurationService.createAsset(modelConfiguration.clone(), projectId, permission)
			);
		} catch (final IOException e) {
			log.error("Unable to get model configuration from postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}

	/**
	 * Updates an existing model config
	 *
	 * @param id UUID of the model to update
	 * @param config New model config to update with
	 * @return updated project ID with UUID set
	 */
	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model configuration")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model configuration updated.",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ModelConfiguration.class))
			),
			@ApiResponse(
				responseCode = "404",
				description = "There was no model configuration found by this ID",
				content = @Content
			),
			@ApiResponse(
				responseCode = "503",
				description = "There was an issue updating the configuration",
				content = @Content
			)
		}
	)
	public ResponseEntity<ModelConfiguration> updateModelConfiguration(
		@PathVariable("id") final UUID id,
		@RequestBody final ModelConfiguration config
	) {
		final UUID projectId = modelConfigurationService.getProjectIdForAsset(id);
		final Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);
		try {
			config.setId(id);
			final Optional<ModelConfiguration> updated = modelConfigurationService.updateAsset(config, projectId, permission);
			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			log.error("Unable to update model configuration in postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}

	/**
	 * Deletes a model config by id
	 *
	 * @param id id of the model config to delete
	 * @return ResponseDeleted
	 */
	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a model configuration")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Deleted configuration",
				content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDeleted.class)) }
			),
			@ApiResponse(responseCode = "503", description = "An error occurred while deleting", content = @Content)
		}
	)
	public ResponseEntity<ResponseDeleted> deleteModelConfiguration(@PathVariable("id") final UUID id) {
		final UUID projectId = modelConfigurationService.getProjectIdForAsset(id);
		final Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			modelConfigurationService.deleteAsset(id, projectId, permission);
			return ResponseEntity.ok(new ResponseDeleted("ModelConfiguration", id));
		} catch (final IOException e) {
			log.error("Unable to delete model configuration", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable")
			);
		}
	}
}
