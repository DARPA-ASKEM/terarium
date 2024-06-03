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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfigurationLegacy;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema.Permission;

@RequestMapping("/model-configurations")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ModelConfigurationController {
	final ModelConfigurationService modelConfigurationService;
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
						content =
								@Content(
										array =
												@ArraySchema(
														schema = @Schema(implementation = ModelConfiguration.class)))),
				@ApiResponse(
						responseCode = "204",
						description = "There are no errors, but also no model configurations for this user",
						content = @Content),
				@ApiResponse(
						responseCode = "503",
						description = "There was an issue communicating with back-end services",
						content = @Content)
			})
	public ResponseEntity<List<ModelConfiguration>> getModelConfigurations(
			@RequestParam(name = "page-size", defaultValue = "500") final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "1") final Integer page) {

		try {
			final List<ModelConfiguration> modelConfigurations =
					modelConfigurationService.getPublicNotTemporaryAssets(pageSize, page);
			if (modelConfigurations.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(modelConfigurations);
		} catch (final Exception e) {
			log.error("Unable to get model configurations from postgres db", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
					messages.get("postgres.service-unavailable"));
		}
	}

	/**
	 * Gets a specific model configuration by id
	 *
	 * @param id UUID of the specific model configuration to fetch
	 * @param projectId the owning project ID
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
						content =
								@Content(
										mediaType = "application/json",
										schema = @Schema(implementation = ModelConfiguration.class))),
				@ApiResponse(
						responseCode = "404",
						description = "There was no configuration found by this ID",
						content = @Content),
				@ApiResponse(
						responseCode = "403",
						description = "User does not have permissions to this model configuration",
						content = @Content),
				@ApiResponse(
						responseCode = "503",
						description = "There was an issue communicating with back-end services",
						content = @Content)
			})
	public ResponseEntity<ModelConfiguration> getModelConfiguration(
			@PathVariable("id") final UUID id,
			@RequestParam(name = "project-id", required = false) final UUID projectId) {
		final Permission permission =
				projectService.checkPermissionCanRead(currentUserService.get().getId(), projectId);
		try {
			final Optional<ModelConfiguration> modelConfiguration = modelConfigurationService.getAsset(id, permission);
			return modelConfiguration.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
					.build());
		} catch (final Exception e) {
			log.error("Unable to get model configuration from postgres db", e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
					messages.get("postgres.service-unavailable"));
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model configuration")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "201",
						description = "Model configuration created.",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = ModelConfigurationLegacy.class))),
				@ApiResponse(
						responseCode = "503",
						description = "There was an issue creating the configuration",
						content = @Content)
			})
	public ResponseEntity<ModelConfiguration> createModelConfiguration(
			@RequestBody final ModelConfiguration modelConfiguration,
			@RequestParam(name = "project-id", required = false) final UUID projectId) {
		final software.uncharted.terarium.hmiserver.utils.rebac.Schema.Permission permission =
				projectService.checkPermissionCanWrite(currentUserService.get().getId(), projectId);

		try {
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(modelConfigurationService.createAsset(modelConfiguration, permission));
		} catch (final IOException e) {
			log.error("Unable to get model configuration from postgres db", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
				messages.get("postgres.service-unavailable"));
		}
	}
}
