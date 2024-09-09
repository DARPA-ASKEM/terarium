package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.FrameworkService;

@RequestMapping("/models")
@RestController
@RequiredArgsConstructor
public class FrameworkController {

	final FrameworkService frameworkService;

	final ObjectMapper objectMapper;

	@PostMapping("/frameworks")
	@Secured(Roles.USER)
	@Operation(summary = "Create a new model framework")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Model framework created.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelFramework.class)
				)
			)
		}
	)
	ResponseEntity<ModelFramework> createFramework(@RequestBody final ModelFramework framework) {
		final ModelFramework modelFramework = frameworkService.createFramework(framework);

		return ResponseEntity.status(HttpStatus.CREATED).body(modelFramework);
	}

	@GetMapping("/frameworks/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a model framework by ID")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model framework found.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelFramework.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "There was no framework found", content = @Content)
		}
	)
	ResponseEntity<ModelFramework> getFramework(@PathVariable("id") final UUID id) {
		final Optional<ModelFramework> framework = frameworkService.getFramework(id);
		if (framework.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Framework %s not found", id));
		}
		return ResponseEntity.ok(framework.get());
	}

	@PutMapping("/frameworks/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a model framework")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Model framework updated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ModelFramework.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "There was no framework found", content = @Content)
		}
	)
	ResponseEntity<ModelFramework> updateFramework(
		@PathVariable("id") final UUID id,
		@RequestBody final ModelFramework framework
	) {
		framework.setId(id);
		final Optional<ModelFramework> updated = frameworkService.updateFramework(framework);
		if (updated.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(updated.get());
	}

	@DeleteMapping("/frameworks/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an model framework")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Deleted framework",
				content = {
					@Content(
						mediaType = "application/json",
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)
					)
				}
			)
		}
	)
	ResponseEntity<ResponseDeleted> deleteFramework(@PathVariable("id") final UUID id) {
		frameworkService.deleteFramework(id);
		return ResponseEntity.ok(new ResponseDeleted("ModelFramework", id));
	}
}
