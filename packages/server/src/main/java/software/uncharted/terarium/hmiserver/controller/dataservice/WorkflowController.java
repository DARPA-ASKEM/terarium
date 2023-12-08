package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.WorkflowService;

import java.io.IOException;
import java.util.List;

@RequestMapping("/workflows")
@RestController
@Slf4j
@RequiredArgsConstructor
public class WorkflowController {

	final WorkflowService workflowService;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all workflows")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Workflows found.",
			content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class)))
		),
		@ApiResponse(
			responseCode = "204",
			description = "There are no workflows found and no errors occurred",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving workflows from the data store",
			content = @Content
		)
	})
	public ResponseEntity<List<Workflow>> getWorkflows(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {
		try {
			final List<Workflow> workflows = workflowService.getWorkflows(pageSize, page);
			if (workflows.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(workflows);
		} catch (final IOException e) {
			final String error = "Unable to retrieve workflows from the data store";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets workflow by ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Workflow found.",
			content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class))
		),
		@ApiResponse(responseCode = "204",
			description = "There was no workflow found but no errors occurred",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving the workflow from the data store",
			content = @Content
		)
	})
	public ResponseEntity<Workflow> getWorkflow(
		@PathVariable("id") final String id
	) {
		try {
			final Workflow workflow = workflowService.getWorkflow(id);
			if (workflow == null) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(workflow);
		} catch (final IOException e) {
			final String error = "Unable to retrieve workflow from the data store";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new workflow")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Workflow created.",
			content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue creating the workflow",
			content = @Content
		)
	})
	public ResponseEntity<Workflow> createWorkflow(@RequestBody final Workflow item) {
		try {
			return ResponseEntity.ok(workflowService.createWorkflow(item));
		} catch (final IOException e) {
			final String error = "Unable to create workflow";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a workflow")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Workflow updated.",
			content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue updating the workflow",
			content = @Content
		)
	})
	public ResponseEntity<Workflow> updateWorkflow(
		@PathVariable("id") final String id,
		@RequestBody final Workflow workflow
	) {
		try {
			return ResponseEntity.ok(workflowService.updateWorkflow(id, workflow));
		} catch (final IOException e) {
			final String error = "Unable to update workflow";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error
			);
		} catch (final IllegalArgumentException e) {
			final String error = "ID does not match Workflow object ID";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.BAD_REQUEST,
				error
			);
		}
	}
}

