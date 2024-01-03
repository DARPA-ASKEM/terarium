package software.uncharted.terarium.hmiserver.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

import java.util.Optional;
import java.util.UUID;

@RequestMapping("/internal")
@RestController
@Slf4j
@RequiredArgsConstructor
@Tags(@Tag(name = "Internal", description = "Insecure internal access"))
public class InternalController {
	public static final String PATH = "/internal";

	final ProjectService projectService;

	@Operation(summary = "Gets a project by ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Project found.", content = {
			@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Project.class)) }),
		@ApiResponse(responseCode = "500", description = "Error finding project", content = @Content),
		@ApiResponse(responseCode = "404", description = "Project not found", content = @Content) })
	@GetMapping("/projects/{id}")
	public ResponseEntity<Project> getProject(
		@PathVariable("id") final UUID id) {
		final Optional<Project> project = projectService.getProject(id);
		if (project.isPresent()) {
			return ResponseEntity.ok(project.get());
		}
		return ResponseEntity.notFound().build();
	}
}
