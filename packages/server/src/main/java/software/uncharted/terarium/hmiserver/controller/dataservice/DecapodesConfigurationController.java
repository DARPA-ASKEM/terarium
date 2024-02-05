package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Decapodes configuration found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesConfiguration.class))),
			@ApiResponse(responseCode = "204", description = "There was no decapodes configuration found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the decapodes configuration from the data store", content = @Content)
	})
	ResponseEntity<DecapodesConfiguration> getDecapodesConfiguration(@PathVariable("id") UUID id) {

		try {

			// Fetch the decapodes configuration from the data-service
			Optional<DecapodesConfiguration> configuration = decapodesConfigurationService.getDecapodesConfiguration(id);
			if (configuration.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			// Return the configuration
			return ResponseEntity.ok(configuration.get());
		} catch (IOException e) {
			final String error = "Unable to get decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a decapodes configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Decapodes configuraiton updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesConfiguration.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the decapodes configuration", content = @Content)
	})
	ResponseEntity<DecapodesConfiguration> updateDecapodesConfiguration(
			@PathVariable("id") UUID id,
			@RequestBody DecapodesConfiguration configuration) {

		try {
			configuration.setId(id);
			final Optional<DecapodesConfiguration> updated = decapodesConfigurationService.updateDecapodesConfiguration(configuration);
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (IOException e) {
			final String error = "Unable to update decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an decapodes configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted decapodes configuration", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "DecapodesConfiguration could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	ResponseEntity<ResponseDeleted> deleteDecapodesConfiguration(
			@PathVariable("id") UUID id) {

		try {
			decapodesConfigurationService.deleteDecapodesConfiguration(id);
			return ResponseEntity.ok(new ResponseDeleted("DecapodesConfiguration", id));
		} catch (IOException e) {
			final String error = "Unable to delete decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new decapodes configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Decapodes configuration created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesConfiguration.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the decapodes configuration", content = @Content)
	})
	ResponseEntity<DecapodesConfiguration> createDecapodesConfiguration(
			@RequestBody DecapodesConfiguration config) {

		try {
			config = decapodesConfigurationService.createDecapodesConfiguration(config);
			return ResponseEntity.status(HttpStatus.CREATED).body(config);
		} catch (IOException e) {
			final String error = "Unable to create decapodes configuration";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}
}
