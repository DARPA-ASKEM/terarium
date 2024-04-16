package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
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
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;

@RequestMapping("/model-configurations")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ModelConfigurationController {

	final ModelConfigurationService modelConfigurationService;
	final ObjectMapper objectMapper;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all model configurations")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model configuration found.",
						content =
								@Content(
										array =
												@ArraySchema(
														schema =
																@io.swagger.v3.oas.annotations.media.Schema(
																		implementation = ModelConfiguration.class)))),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving configuration from the data store",
						content = @Content)
			})
	public ResponseEntity<List<ModelConfiguration>> getModelConfigurations(
			@RequestParam(name = "page-size", defaultValue = "500") final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0") final Integer page) {

		try {
			return ResponseEntity.ok(modelConfigurationService.getAssets(pageSize, page));
		} catch (final IOException e) {
			final String error = "Unable to get model configurations";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
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
														implementation = ModelConfiguration.class))),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue creating the configuration",
						content = @Content)
			})
	public ResponseEntity<ModelConfiguration> createModelConfiguration(@RequestBody final ModelConfiguration config) {

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(modelConfigurationService.createAsset(config));
		} catch (final IOException e) {
			final String error = "Unable to create model configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model configuration by ID")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model configuration found.",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = ModelConfiguration.class))),
				@ApiResponse(
						responseCode = "404",
						description = "There was no configuration found",
						content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving the configuration from the data store",
						content = @Content)
			})
	public ResponseEntity<ModelConfiguration> getModelConfiguration(@PathVariable("id") final UUID id) {

		try {
			final Optional<ModelConfiguration> modelConfiguration = modelConfigurationService.getAsset(id);
			return modelConfiguration.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
					.build());
		} catch (final IOException e) {
			final String error = "Unable to get model configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a model configuration")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Model configuration updated.",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = ModelConfiguration.class))),
				@ApiResponse(
						responseCode = "404",
						description = "Model configuration could not be found",
						content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue updating the configuration",
						content = @Content)
			})
	public ResponseEntity<ModelConfiguration> updateModelConfiguration(
			@PathVariable("id") final UUID id, @RequestBody final ModelConfiguration config) {

		try {
			config.setId(id);
			final Optional<ModelConfiguration> updated = modelConfigurationService.updateAsset(config);
			return updated.map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to update model configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an model configuration")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Deleted configuration",
						content = {
							@Content(
									mediaType = "application/json",
									schema =
											@io.swagger.v3.oas.annotations.media.Schema(
													implementation = ResponseDeleted.class))
						}),
				@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
			})
	public ResponseEntity<ResponseDeleted> deleteModelConfiguration(@PathVariable("id") final UUID id) {

		try {
			modelConfigurationService.deleteAsset(id);
			return ResponseEntity.ok(new ResponseDeleted("ModelConfiguration", id));
		} catch (final IOException e) {
			final String error = "Unable to delete model configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
