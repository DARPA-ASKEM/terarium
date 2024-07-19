package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.multiphysics.DecapodesConfiguration;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DecapodesConfigurationService;

@RequestMapping("/decapodes-configurations")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DecapodesConfigurationController {

	final DecapodesConfigurationService decapodesConfigurationService;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a decapodes configuration by ID")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Decapodes configuration found.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesConfiguration.class)
				)
			),
			@ApiResponse(
				responseCode = "204",
				description = "There was no decapodes configuration found",
				content = @Content
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the decapodes configuration from the data store",
				content = @Content
			)
		}
	)
	ResponseEntity<DecapodesConfiguration> getDecapodesConfiguration(@PathVariable("id") final UUID id) {
		try {
			// Fetch the decapodes configuration from the data-service
			final Optional<DecapodesConfiguration> configuration = decapodesConfigurationService.getDecapodesConfiguration(
				id
			);
			return configuration.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
			// Return the configuration
		} catch (final IOException e) {
			final String error = "Unable to get decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a decapodes configuration")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Decapodes configuraiton updated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesConfiguration.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "DecapodesConfiguration could not be found", content = @Content),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue updating the decapodes configuration",
				content = @Content
			)
		}
	)
	ResponseEntity<DecapodesConfiguration> updateDecapodesConfiguration(
		@PathVariable("id") final UUID id,
		@RequestBody final DecapodesConfiguration configuration
	) {
		try {
			configuration.setId(id);
			final Optional<DecapodesConfiguration> updated = decapodesConfigurationService.updateDecapodesConfiguration(
				configuration
			);
			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to update decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an decapodes configuration")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Deleted decapodes configuration",
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
	ResponseEntity<ResponseDeleted> deleteDecapodesConfiguration(@PathVariable("id") final UUID id) {
		try {
			decapodesConfigurationService.deleteDecapodesConfiguration(id);
			return ResponseEntity.ok(new ResponseDeleted("DecapodesConfiguration", id));
		} catch (final IOException e) {
			final String error = "Unable to delete decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new decapodes configuration")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Decapodes configuration created.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesConfiguration.class)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue creating the decapodes configuration",
				content = @Content
			)
		}
	)
	ResponseEntity<DecapodesConfiguration> createDecapodesConfiguration(@RequestBody DecapodesConfiguration config) {
		try {
			config = decapodesConfigurationService.createDecapodesConfiguration(config);
			return ResponseEntity.status(HttpStatus.CREATED).body(config);
		} catch (final IOException e) {
			final String error = "Unable to create decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
