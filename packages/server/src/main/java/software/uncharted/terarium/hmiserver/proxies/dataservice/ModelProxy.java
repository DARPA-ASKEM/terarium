package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelFramework;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelOperationCopy;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;


import java.util.List;


@FeignClient(name = "models", url = "${terarium.dataservice.url}", path = "/models")
public interface ModelProxy {

	@PostMapping("/frameworks")
	ResponseEntity<JsonNode> createFramework(
		@RequestBody ModelFramework framework
	);

	@GetMapping("/frameworks/{name}")
	ResponseEntity<JsonNode> getFramework(
		@PathVariable("name") String name
	);

	@DeleteMapping("/frameworks/{name}")
	ResponseEntity<JsonNode> deleteFramework(
		@PathVariable("name") String name
	);

	@GetMapping("/descriptions")
	ResponseEntity<JsonNode> getDescriptions(
		@RequestParam(name = "page_size", defaultValue = "100") Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0") Integer page
	);

	@GetMapping("/{id}/descriptions")
	ResponseEntity<JsonNode> getDescription(
		@PathVariable("id") String id
	);

	@GetMapping("/{id}")
	ResponseEntity<Model> getModel(
		@PathVariable("id") String id
	);

	@PutMapping("/{id}")
	ResponseEntity<JsonNode> updateModel(
		@PathVariable("id") String id,
		@RequestBody Model model
	);

	@PostMapping
	ResponseEntity<JsonNode> createModel(
		@RequestBody Model model
	);


	@GetMapping("/{id}/model_configurations")
	ResponseEntity<List<ModelConfiguration>> getModelConfigurations(
			@PathVariable("id") String id,
			@RequestParam("page_size") int pageSize
	);
}
