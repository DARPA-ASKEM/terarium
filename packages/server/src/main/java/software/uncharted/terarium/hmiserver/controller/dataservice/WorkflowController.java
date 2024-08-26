package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.WorkflowService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/workflows")
@RestController
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WorkflowController {

	final WorkflowService workflowService;

	final ProjectAssetService projectAssetService;

	final ProjectService projectService;

	final CurrentUserService currentUserService;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all workflows")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Workflows found.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class))
				)
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
		}
	)
	public ResponseEntity<List<Workflow>> getWorkflows(
		@RequestParam(name = "page-size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {
		final List<Workflow> workflows = workflowService.getPublicNotTemporaryAssets(page, pageSize);
		if (workflows.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(workflows);
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets workflow by ID")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Workflow found.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class)
				)
			),
			@ApiResponse(responseCode = "204", description = "There was no workflow found", content = @Content),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the workflow from the data store",
				content = @Content
			)
		}
	)
	public ResponseEntity<Workflow> getWorkflow(@PathVariable("id") final UUID id) {
		final UUID projectId = workflowService.getProjectIdForAsset(id);
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);

		final Optional<Workflow> workflow = workflowService.getAsset(id, permission);
		return workflow.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new workflow")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Workflow created.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the workflow", content = @Content)
		}
	)
	public ResponseEntity<Workflow> createWorkflow(
		@RequestBody final Workflow workflow,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(
				workflowService.createAsset(workflow, projectId, permission)
			);
		} catch (final IOException e) {
			final String error = "Unable to create workflow";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a workflow")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Workflow updated.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Workflow.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "Workflow could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the workflow", content = @Content)
		}
	)
	public ResponseEntity<Workflow> updateWorkflow(
		@PathVariable("id") final UUID id,
		@RequestBody final Workflow workflow
	) {
		final UUID projectId = workflowService.getProjectIdForAsset(id);
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);
		try {
			workflow.setId(id);

			final Optional<Workflow> updated = workflowService.updateAsset(workflow, projectId, permission);
			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to update workflow";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		} catch (final IllegalArgumentException e) {
			final String error = "ID does not match Workflow object ID";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Delete a workflow by ID")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Delete workflow",
				content = {
					@Content(
						mediaType = "application/json",
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)
					)
				}
			),
			@ApiResponse(responseCode = "500", description = "There was an issue deleting the workflow", content = @Content)
		}
	)
	public ResponseEntity<ResponseDeleted> deleteWorkflow(@PathVariable("id") final UUID id) {
		final UUID projectId = workflowService.getProjectIdForAsset(id);
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			workflowService.deleteAsset(id, projectId, permission);
			return ResponseEntity.ok(new ResponseDeleted("Workflow", id));
		} catch (final Exception e) {
			final String error = String.format("Failed to delete workflow %s", id);
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
