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
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;

@RequestMapping("/model_configurations")
@RestController
@RequiredArgsConstructor
public class ModelConfigurationController {

	final ModelConfigurationService modelConfigurationService;
	final ObjectMapper objectMapper;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<ModelConfiguration>> getModelConfigurations(
			@RequestParam(name = "page_size", defaultValue = "500") final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0") final Integer page) throws IOException {
		return ResponseEntity.ok(modelConfigurationService.getModelConfigurations(pageSize, page));
	}

	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<ResponseId> createModelConfiguration(@RequestBody ModelConfiguration config)
			throws IOException {

		ModelConfiguration modelConfiguration = modelConfigurationService.createModelConfiguration(config);
		return ResponseEntity.ok(new ResponseId().setId(modelConfiguration.getId()));
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<ModelConfiguration> getModelConfiguration(
			@PathVariable("id") String id) throws IOException {

		return ResponseEntity.ok(modelConfigurationService.getModelConfiguration(id));
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseId> updateModelConfiguration(
			@PathVariable("id") String id,
			@RequestBody ModelConfiguration config) throws IOException {

		modelConfigurationService.updateModelConfiguration(config.setId(id));
		return ResponseEntity.ok(new ResponseId().setId(id));
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseDeleted> deleteModelConfiguration(
			@PathVariable("id") String id) throws IOException {
		modelConfigurationService.deleteModelConfiguration(id);

		return ResponseEntity.ok(new ResponseDeleted("ModelConfiguration", id));
	}
}
