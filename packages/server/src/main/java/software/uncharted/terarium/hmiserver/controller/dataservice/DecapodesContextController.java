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
import software.uncharted.terarium.hmiserver.models.dataservice.multiphysics.DecapodesContext;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DecapodesContextService;

@RequestMapping("/decapodes-contexts")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DecapodesContextController {

	final DecapodesContextService decapodesContextService;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a decapodes context by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Decapodes context found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesContext.class))),
			@ApiResponse(responseCode = "204", description = "There was no decapodes context found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the decapodes context from the data store", content = @Content)
	})
	ResponseEntity<DecapodesContext> getDecapodesContext(@PathVariable("id") UUID id) {

		try {

			// Fetch the decapodes context from the data-service
			Optional<DecapodesContext> context = decapodesContextService.getDecapodesContext(id);
			if (context.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			return ResponseEntity.ok(context.get());
		} catch (IOException e) {
			final String error = "Unable to get decapodes context";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a decapodes context")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Decapodes context updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesContext.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the decapodes context", content = @Content)
	})
	ResponseEntity<DecapodesContext> updateDecapodesContext(
			@PathVariable("id") UUID id,
			@RequestBody DecapodesContext context) {

		try {
			context.setId(id);
			final Optional<DecapodesContext> updated = decapodesContextService.updateDecapodesContext(context);
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (IOException e) {
			final String error = "Unable to update decapodes context";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an decapodes context")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted decapodes context", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "DecapodesContext could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	ResponseEntity<ResponseDeleted> deleteDecapodesContext(
			@PathVariable("id") UUID id) {

		try {
			decapodesContextService.deleteDecapodesContext(id);
			return ResponseEntity.ok(new ResponseDeleted("DecapodesContext", id));
		} catch (IOException e) {
			final String error = "Unable to delete decapodes context";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new decapodes context")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Decapodes context created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DecapodesContext.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the decapodes context", content = @Content)
	})
	ResponseEntity<DecapodesContext> createDecapodesContext(
			@RequestBody DecapodesContext context) {

		try {
			context = decapodesContextService.createDecapodesContext(context);
			return ResponseEntity.status(HttpStatus.CREATED).body(context);
		} catch (IOException e) {
			final String error = "Unable to create decapodes context";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}
}
