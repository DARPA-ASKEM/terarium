package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
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

	@PostMapping(value = "/model-to-latex/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> model2Latex(@PathVariable("id") final UUID id) {
		return ResponseEntity
				.ok(simulationServiceProxy.getModelEquation(id.toString()).getBody());
	}
}
