package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.FrameworkService;

@RequestMapping("/models")
@RestController
@Slf4j
@RequiredArgsConstructor
public class FrameworkController {

	final FrameworkService frameworkService;

	final ObjectMapper objectMapper;

	@PostMapping("/frameworks")
	@Secured(Roles.USER)
	ResponseEntity<JsonNode> createFramework(
			@RequestBody final ModelFramework framework) {

		ModelFramework modelFramework = frameworkService.createFramework(framework);

		JsonNode res = objectMapper.valueToTree(Map.of("id", modelFramework.getId()));

		return ResponseEntity.ok(res);
	}

	@GetMapping("/frameworks/{name}")
	@Secured(Roles.USER)
	ResponseEntity<ModelFramework> getFramework(
			@PathVariable("name") String name) {

		Optional<ModelFramework> framework = frameworkService.getFrameworkByName(name);
		if (framework.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Document %s not found", name));
		}

		return ResponseEntity.ok(framework.get());
	}

	@DeleteMapping("/frameworks/{name}")
	@Secured(Roles.USER)
	ResponseEntity<JsonNode> deleteFramework(
			@PathVariable("name") String name) {

		frameworkService.deleteFrameworkByName(name);

		JsonNode res = objectMapper
				.valueToTree(Map.of("message", String.format("ModelFramework successfully deleted: %s", name)));

		return ResponseEntity.ok(res);
	}

}
