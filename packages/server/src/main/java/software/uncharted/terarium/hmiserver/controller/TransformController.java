package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.List;
import java.util.UUID;


@RequestMapping("/transforms")
@RestController
@Slf4j
public class TransformController {

	@Autowired
	SkemaRustProxy skemaProxy;

	@Autowired
	SimulationServiceProxy simulationServiceProxy;

	@PostMapping("/mathml-to-acset")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> mathML2ACSet(@RequestBody final List<String> list) {
		return skemaProxy.convertMathML2ACSet(list);
	}

	@GetMapping("/model-to-latex/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> model2Latex(@PathVariable("id") final UUID id) {
		try {
			return ResponseEntity
				.ok(simulationServiceProxy.getModelEquation(id.toString()).getBody());
		} catch (final Exception error) {
			log.error("Error getting latex from simulation service", error);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Simulation Service is unable to provide the set of LaTeX equations from the Model.");
		}
	}
}
