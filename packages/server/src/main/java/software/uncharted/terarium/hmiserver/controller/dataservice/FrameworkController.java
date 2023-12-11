package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.Optional;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.FrameworkService;

@RequestMapping("/models")
@RestController
@Slf4j
@RequiredArgsConstructor
public class FrameworkController {

	final FrameworkService frameworkService;

	final ObjectMapper objectMapper;

	@PostMapping("/frameworks")
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model framework")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model framework created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the framework", content = @Content)
	})
	ResponseEntity<ResponseId> createFramework(
			@RequestBody final ModelFramework framework) {
		ModelFramework modelFramework = frameworkService.createFramework(framework);

		return ResponseEntity.ok(new ResponseId(modelFramework.getId()));
	}

	@GetMapping("/frameworks/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model framework by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model framework found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelFramework.class))),
			@ApiResponse(responseCode = "204", description = "There was no framework found but no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the framework from the data store", content = @Content)
	})
	ResponseEntity<ModelFramework> getFramework(
			@PathVariable("id") UUID id) {

		Optional<ModelFramework> framework = frameworkService.getFramework(id);
		if (framework.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Document %s not found", id));
		}
		return ResponseEntity.ok(framework.get());
	}

	@PutMapping("/frameworks/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a model framework")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Model framework updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the framework", content = @Content)
	})
	ResponseEntity<ResponseId> updateFramework(
			@PathVariable("id") String id,
			@RequestBody final ModelFramework framework) {

		frameworkService.updateFramework(framework);
		return ResponseEntity.ok(new ResponseId(id));
	}

	@DeleteMapping("/frameworks/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an model framework")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted framework", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "Model framework could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	ResponseEntity<ResponseDeleted> deleteFramework(
			@PathVariable("id") UUID id) {

		frameworkService.deleteFramework(id);
		return ResponseEntity.ok(new ResponseDeleted("ModelFramework", id));
	}

}
