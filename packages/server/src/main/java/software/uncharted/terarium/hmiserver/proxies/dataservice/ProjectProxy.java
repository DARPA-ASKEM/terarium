package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.data.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;

import java.util.List;
import java.util.UUID;


@FeignClient(name = "dataservice", url = "${terarium.dataservice.url}", path = "/projects")
public interface ProjectProxy {

	@GetMapping
	ResponseEntity<List<Project>> getProjects(
		@RequestParam(name = "include_inactive", defaultValue = "false") final Boolean includeInactive
	);


	@GetMapping("/{id}")
	ResponseEntity<Project> getProject(
		@PathVariable("id") UUID id
	);

	@PostMapping
	ResponseEntity<JsonNode> createProject(
		@RequestBody Project project
	);


	@PutMapping("/{id}")
	ResponseEntity<JsonNode> updateProject(
		@PathVariable("id") UUID id,
		@RequestBody Project project
	);

	@DeleteMapping("/{id}")
	ResponseEntity<JsonNode> deleteProject(
		@PathVariable("id") UUID id
	);

	@GetMapping("/{project_id}/assets")
	ResponseEntity<Assets> getAssets(
		@PathVariable("project_id") UUID projectId,
		@RequestParam("types") final List<AssetType> types
	);

	@PostMapping("/{project_id}/assets/{resource_type}/{resource_id}")
	ResponseEntity<JsonNode> createAsset(
		@PathVariable("project_id") UUID projectId,
		@PathVariable("resource_type") AssetType type,
		@PathVariable("resource_id") String resourceId
	);

	@DeleteMapping("/{project_id}/assets/{resource_type}/{resource_id}")
	ResponseEntity<JsonNode> deleteAsset(
		@PathVariable("project_id") UUID projectId,
		@PathVariable("resource_type") AssetType type,
		@PathVariable("resource_id") String resourceId
	);
}
