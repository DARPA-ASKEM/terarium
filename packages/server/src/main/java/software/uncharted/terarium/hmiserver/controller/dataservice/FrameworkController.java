package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.Optional;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
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
	ResponseEntity<ResponseId> createFramework(
			@RequestBody final ModelFramework framework) {
		ModelFramework modelFramework = frameworkService.createFramework(framework);

		return ResponseEntity.ok(new ResponseId().setId(modelFramework.getName()));
	}

	@GetMapping("/frameworks/{name}")
	@Secured(Roles.USER)
	ResponseEntity<ModelFramework> getFramework(
			@PathVariable("name") String name) {

		Optional<ModelFramework> framework = frameworkService.getFramework(name);
		if (framework.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Document %s not found", name));
		}
		return ResponseEntity.ok(framework.get());
	}

	@PutMapping("/frameworks/{name}")
	@Secured(Roles.USER)
	ResponseEntity<ResponseId> updateFramework(
			@PathVariable("name") String name,
			@RequestBody final ModelFramework framework) {

		frameworkService.updateFramework(framework.setName(name));
		return ResponseEntity.ok(new ResponseId().setId(name));
	}

	@DeleteMapping("/frameworks/{name}")
	@Secured(Roles.USER)
	ResponseEntity<ResponseDeleted> deleteFramework(
			@PathVariable("name") String name) {

		frameworkService.deleteFramework(name);
		return ResponseEntity.ok(new ResponseDeleted("ModelFramework", name));
	}

}
