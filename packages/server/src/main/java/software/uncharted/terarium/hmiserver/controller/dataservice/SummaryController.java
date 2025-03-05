package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.Summary;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.SummaryService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/summary")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SummaryController {

	final SummaryService summaryService;

	final ProjectAssetService projectAssetService;

	final ProjectService projectService;

	final CurrentUserService currentUserService;

	@PostMapping("/search")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a map of summaries by list of IDs")
	@HasProjectAccess
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Summaries found.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Summary.class)
				)
			),
			@ApiResponse(responseCode = "204", description = "There was no summary found", content = @Content),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the summaries from the data store",
				content = @Content
			)
		}
	)
	public ResponseEntity<Map<UUID, Summary>> getSummaryMap(
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestBody final List<UUID> ids
	) {
		final List<Summary> summaries = summaryService.getSummaries(ids);

		if (summaries.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		final Map<UUID, Summary> summaryMap = new HashMap<>();
		for (final Summary summary : summaries) {
			summaryMap.put(summary.getId(), summary);
		}

		return ResponseEntity.ok(summaryMap);
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets summary by ID")
	@HasProjectAccess
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Summary found.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Summary.class)
				)
			),
			@ApiResponse(responseCode = "204", description = "There was no summary found", content = @Content),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the summary from the data store",
				content = @Content
			)
		}
	)
	public ResponseEntity<Summary> getSummary(
		@PathVariable("id") final UUID id,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Optional<Summary> summary = summaryService.getAsset(id);
		return summary.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new summary")
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Summary created.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Summary.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the summary", content = @Content)
		}
	)
	public ResponseEntity<Summary> createSummary(
		@RequestBody final Summary item,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(summaryService.createAsset(item, projectId));
		} catch (final IOException e) {
			final String error = "Unable to create summary";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a summary")
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Summary updated.",
				content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Summary.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "Summary could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the summary", content = @Content)
		}
	)
	public ResponseEntity<Summary> updateSummary(
		@PathVariable("id") final UUID id,
		@RequestBody final Summary summary,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			summary.setId(id);
			final Optional<Summary> updated = summaryService.updateAsset(summary, projectId);
			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to update summary";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		} catch (final IllegalArgumentException e) {
			final String error = "ID does not match Summary object ID";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Delete a summary by ID")
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Delete summary",
				content = {
					@Content(
						mediaType = "application/json",
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)
					)
				}
			),
			@ApiResponse(responseCode = "500", description = "There was an issue deleting the summary", content = @Content)
		}
	)
	public ResponseEntity<ResponseDeleted> deleteSummary(
		@PathVariable("id") final UUID id,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		try {
			summaryService.deleteAsset(id, projectId);
			return ResponseEntity.ok(new ResponseDeleted("Summary", id));
		} catch (final Exception e) {
			final String error = String.format("Failed to delete summary %s", id);
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
