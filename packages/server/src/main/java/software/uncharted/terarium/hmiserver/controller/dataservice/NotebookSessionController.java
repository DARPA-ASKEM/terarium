package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.io.IOException;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.NotebookSessionService;

/**
 * Rest controller for storing, retrieving, modifying and deleting notebook
 * sessions in the dataservice
 */
@RequestMapping("/sessions")
@RestController
@Slf4j
@RequiredArgsConstructor
public class NotebookSessionController {

	final NotebookSessionService sessionService;
	final ObjectMapper objectMapper;

	/**
	 * Retrieve the list of NotebookSessions
	 *
	 * @param pageSize number of sessions per page
	 * @param page     current page number
	 * @return list of sessions
	 */
	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all sessions")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "NotebookSessions found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NotebookSession.class)))),
			@ApiResponse(responseCode = "204", description = "There are no sessions found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving sessions from the data store", content = @Content)
	})
	ResponseEntity<List<NotebookSession>> getNotebookSessions(
			@RequestParam(name = "page-size", defaultValue = "100") Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0") Integer page) {

		try {
			return ResponseEntity.ok(sessionService.getNotebookSessions(pageSize, page));
		} catch (IOException e) {
			final String error = "Unable to get sessions";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Create session and return its ID
	 *
	 * @param session session to create
	 * @return new ID for session
	 */
	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "NotebookSession created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NotebookSession.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the session", content = @Content)
	})
	ResponseEntity<NotebookSession> createNotebookSession(@RequestBody NotebookSession session) {

		try {
			sessionService.createNotebookSession(session);
			return ResponseEntity.status(HttpStatus.CREATED).body(session);
		} catch (IOException e) {
			final String error = "Unable to create session";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Retrieve an session
	 *
	 * @param id session id
	 * @return NotebookSession
	 */
	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets session by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "NotebookSession found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NotebookSession.class))),
			@ApiResponse(responseCode = "204", description = "There was no session found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the session from the data store", content = @Content)
	})
	ResponseEntity<NotebookSession> getNotebookSession(@PathVariable("id") UUID id) {

		try {
			Optional<NotebookSession> session = sessionService.getNotebookSession(id);
			if (session.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(session.get());
		} catch (IOException e) {
			final String error = "Unable to get session";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Update an session
	 *
	 * @param id      id of session to update
	 * @param session session to update with
	 * @return ID of updated session
	 */
	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "NotebookSession updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NotebookSession.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the session", content = @Content)
	})
	ResponseEntity<NotebookSession> updateNotebookSession(
			@PathVariable("id") UUID id,
			@RequestBody NotebookSession session) {

		try {
			final Optional<NotebookSession> updated = sessionService.updateNotebookSession(session.setId(id));
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (IOException e) {
			final String error = "Unable to update session";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Deletes and session
	 *
	 * @param id session to delete
	 * @return delete message
	 */
	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an session")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted session", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "NotebookSession could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	ResponseEntity<ResponseDeleted> deleteNotebookSession(@PathVariable("id") UUID id) {

		try {
			sessionService.deleteNotebookSession(id);
			return ResponseEntity.ok(new ResponseDeleted("NotebookSession", id));
		} catch (IOException e) {
			final String error = "Unable to delete session";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

}
