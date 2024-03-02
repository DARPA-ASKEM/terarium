package software.uncharted.terarium.hmiserver.controller.simulationservice;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import org.springframework.security.access.annotation.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import java.util.UUID;

@RequestMapping("/model-equation")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ModelEquationRequestController implements SnakeCaseController {

	private final SimulationServiceProxy simulationServiceProxy;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> getSimulation(@PathVariable("id") final UUID id) {
		return ResponseEntity
				.ok(simulationServiceProxy.getModelEquation(id.toString()).getBody());
	}
}
