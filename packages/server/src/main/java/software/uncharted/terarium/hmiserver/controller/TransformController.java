package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/transforms")
@RestController
@Slf4j
@RequiredArgsConstructor
public class TransformController {

	final SkemaRustProxy skemaProxy;

	final SimulationServiceProxy simulationServiceProxy;

	@PostMapping("/mathml-to-acset")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> mathML2ACSet(@RequestBody final List<String> list) {
		try {
			return skemaProxy.convertMathML2ACSet(list);
		} catch (final FeignException error) {
			log.error("Error posting MathML to ACSet", error);
			throw new ResponseStatusException(
					error.status() < 100
							? org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
							: org.springframework.http.HttpStatus.valueOf(error.status()),
					"Error posting MathML to ACSet " + error.getMessage());
		}
	}

	@GetMapping("/model-to-latex/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> model2Latex(@PathVariable("id") final UUID id) {
		try {
			return ResponseEntity.ok(
					simulationServiceProxy.getModelEquation(id.toString()).getBody());
		} catch (final FeignException error) {
			log.error("Error getting latex from simulation service", error);
			throw new ResponseStatusException(
					error.status() < 100
							? org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
							: org.springframework.http.HttpStatus.valueOf(error.status()),
					"Simulation Service is unable to provide the set of LaTeX equations from the Model. "
							+ error.getMessage());
		} catch (final Exception error) {
			log.error("Error getting latex from simulation service", error);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					"Simulation Service is unable to provide the set of LaTeX equations from the Model.");
		}
	}
}
