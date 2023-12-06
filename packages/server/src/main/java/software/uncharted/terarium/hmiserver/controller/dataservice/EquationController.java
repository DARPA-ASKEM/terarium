package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.io.IOException;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;

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
	ResponseEntity<List<Equation>> getEquations(
			@RequestParam(name = "page_size", defaultValue = "100") Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0") Integer page) throws IOException {
		return ResponseEntity.ok(equationService.getEquations(pageSize, page));
	}

	/**
	 * Create equation and return its ID
	 *
	 * @param equation equation to create
	 * @return new ID for equation
	 */
	@PostMapping
	@Secured(Roles.USER)
	ResponseEntity<ResponseId> createEquation(@RequestBody Equation equation) throws IOException {
		equationService.createEquation(equation);
		return ResponseEntity.ok(new ResponseId().setId(equation.getId()));
	}

	/**
	 * Retrieve an equation
	 *
	 * @param id equation id
	 * @return Equation
	 */
	@GetMapping("/{equation_id}")
	@Secured(Roles.USER)
	ResponseEntity<Equation> getEquation(@PathVariable("equation_id") String id) throws IOException {
		return ResponseEntity.ok(equationService.getEquation(id));
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
	ResponseEntity<ResponseId> updateEquation(@PathVariable("equation_id") String id, @RequestBody Equation equation)
			throws IOException {
		equationService.updateEquation(equation.setId(id));
		return ResponseEntity.ok(new ResponseId().setId(id));
	}

	/**
	 * Deletes and equation
	 *
	 * @param id equation to delete
	 * @return delete message
	 */
	@DeleteMapping("/{equation_id}")
	@Secured(Roles.USER)
	ResponseEntity<ResponseDeleted> deleteEquation(@PathVariable("equation_id") String id) throws IOException {
		equationService.deleteEquation(id);
		return ResponseEntity.ok(new ResponseDeleted("Equation", id));
	}

}
