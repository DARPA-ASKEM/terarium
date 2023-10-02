package software.uncharted.terarium.hmiserver.controller.dataservice;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseResource;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelConfigurationProxy;

import java.util.List;


@RequestMapping("/model_configurations")
@RestController

public class ModelConfigurationResource implements SnakeCaseResource {

	@Autowired
	ModelConfigurationProxy proxy;

	@GetMapping
	public ResponseEntity<List<ModelConfiguration>> getModelConfigurations(
		@RequestParam(name = "page_size", defaultValue = "500") final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0") final Integer page
	) {
		return ResponseEntity.ok(proxy.getAssets(pageSize, page).getBody());
	}

	//TODO why isnt the param here a ModelConfiguration?
	@PostMapping
	public ResponseEntity<JsonNode> createModelConfiguration(Object config) {
		return ResponseEntity.ok(proxy.createAsset(convertObjectToSnakeCaseJsonNode(config)).getBody());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ModelConfiguration> getModelConfiguration(
		@PathVariable("id") String id
	) {
		return ResponseEntity.ok(proxy.getAsset(id).getBody());
	}

	@PutMapping("/{id}")
	public ResponseEntity<JsonNode> updateModelConfiguration(
		@PathVariable("id") String id,
		@RequestBody ModelConfiguration config
	) {
		return ResponseEntity.ok(proxy.updateAsset(id, convertObjectToSnakeCaseJsonNode(config)).getBody());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<JsonNode> deleteModelConfiguration(
		@PathVariable("id") String id
	) {
		return ResponseEntity.ok(proxy.deleteAsset(id).getBody());
	}
}
