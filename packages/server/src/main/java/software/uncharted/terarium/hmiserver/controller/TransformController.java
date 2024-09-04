package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.utils.Messages;

@RequestMapping("/transforms")
@RestController
@Slf4j
@RequiredArgsConstructor
public class TransformController {

	final SimulationServiceProxy simulationServiceProxy;
	final Messages messages;

	@GetMapping("/model-to-latex/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> model2Latex(@PathVariable("id") final UUID id) {
		try {
			return ResponseEntity.ok(simulationServiceProxy.getModelEquation(id.toString()).getBody());
		} catch (final FeignException error) {
			final HttpStatus statusCode = HttpStatus.resolve(error.status());
			if (statusCode != null && statusCode.is4xxClientError()) {
				log.warn(String.format("SciML can't extract LaTeX equations for model: %s", id), error);
				throw new ResponseStatusException(statusCode, messages.get("sciml.internal-error"));
			} else if (statusCode != null && statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
				log.warn("SciML is currently unavailable");
				throw new ResponseStatusException(statusCode, messages.get("sciml.service-unavailable"));
			} else if (statusCode != null && statusCode.is5xxServerError()) {
				log.error(String.format("An error occurred while SciML was extract LaTeX equations for model: %s", id), error);
				throw new ResponseStatusException(statusCode, messages.get("sciml.internal-error"));
			}
			log.error(
				String.format("an unknown error occurred while SciML was extract LaTeX equations for model: %s", id),
				error
			);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.unknown"));
		}
	}
}
