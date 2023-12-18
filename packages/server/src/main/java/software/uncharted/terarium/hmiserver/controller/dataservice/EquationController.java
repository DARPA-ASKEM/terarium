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
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.EquationService;

/**
 * Rest controller for storing, retrieving, modifying and deleting equation
 * assets in the dataservice
 */
@RequestMapping("/equations")
@RestController
@Slf4j
@RequiredArgsConstructor
public class EquationController {

	final EquationService equationService;
	final ObjectMapper objectMapper;

	/**
	 * Retrieve the list of Equations
	 *
	 * @param pageSize number of equations per page
	 * @param page     current page number
	 * @return list of equations
	 */
	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all equations")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Equations found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Equation.class)))),
			@ApiResponse(responseCode = "204", description = "There are no equations found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving equations from the data store", content = @Content)
	})
	ResponseEntity<List<Equation>> getEquations(
			@RequestParam(name = "page_size", defaultValue = "100") Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0") Integer page) {

		try {
			return ResponseEntity.ok(equationService.getEquations(pageSize, page));
		} catch (IOException e) {
			final String error = "Unable to get equations";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Create equation and return its ID
	 *
	 * @param equation equation to create
	 * @return new ID for equation
	 */
	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new equation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Equation created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the equation", content = @Content)
	})
	ResponseEntity<Equation> createEquation(@RequestBody Equation equation) {

		try {
			equation = equationService.createEquation(equation);
			return ResponseEntity.status(HttpStatus.CREATED).body(equation);
		} catch (IOException e) {
			final String error = "Unable to create equation";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Retrieve an equation
	 *
	 * @param id equation id
	 * @return Equation
	 */
	@GetMapping("/{equation_id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets equation by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Equation found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Equation.class))),
			@ApiResponse(responseCode = "204", description = "There was no equation found but no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the equation from the data store", content = @Content)
	})
	ResponseEntity<Equation> getEquation(@PathVariable("equation_id") UUID id) {

		try {
			return ResponseEntity.ok(equationService.getEquation(id));
		} catch (IOException e) {
			final String error = "Unable to get equation";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Update an equation
	 *
	 * @param id       id of equation to update
	 * @param equation equation to update with
	 * @return ID of updated equation
	 */
	@PutMapping("/{equation_id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a equation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Equation updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the equation", content = @Content)
	})
	ResponseEntity<Equation> updateEquation(
			@PathVariable("equation_id") UUID id,
			@RequestBody Equation equation) {

		try {
			final Optional<Equation> updated = equationService.updateEquation(equation.setId(id));
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (IOException e) {
			final String error = "Unable to update equation";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Deletes and equation
	 *
	 * @param id equation to delete
	 * @return delete message
	 */
	@DeleteMapping("/{equation_id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an equation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Deleted equation", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "Equation could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	ResponseEntity<ResponseDeleted> deleteEquation(@PathVariable("equation_id") UUID id) {

		try {
			equationService.deleteEquation(id);
			return ResponseEntity.ok(new ResponseDeleted("Equation", id));
		} catch (IOException e) {
			final String error = "Unable to delete equation";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

}
