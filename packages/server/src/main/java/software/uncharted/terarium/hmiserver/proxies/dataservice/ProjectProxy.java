package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;

import javax.ws.rs.*;
import java.util.*;


@FeignClient(name = "dataservice", url = "${terarium.dataservice.url}", path = "/projects")
public interface ProjectProxy {

	@GetMapping
	ResponseEntity<List<Project>> getProjects(
		@RequestParam(name = "include_inactive", defaultValue="false") final Boolean includeInactive
	);


	@GetMapping("/{id}")
	ResponseEntity<Project> getProject(
		@PathVariable("id") String id
	);

	@PostMapping
	ResponseEntity<JsonNode> createProject(
		@RequestBody Project project
	);


	@PutMapping("/{id}")
	ResponseEntity<JsonNode> updateProject(
		@PathVariable("id") String id,
		Project project
	);

	@DeleteMapping("/{id}")
	ResponseEntity<JsonNode> deleteProject(
		@PathVariable("id") String id
	);

	@GetMapping("/{project_id}/assets")
	ResponseEntity<Assets> getAssets(
		@PathVariable("project_id") String projectId,
		@RequestParam("types") final List<Assets.AssetType> types
	);

	@PostMapping("/{id}/assets/{resource_type}/{resource_id}")
	ResponseEntity<JsonNode> createAsset(
		@PathVariable("id") String id,
		@PathVariable("resource_type") Assets.AssetType type,
		@PathVariable("resource_id") String resourceId
	);

	@DeleteMapping("/{project_id}/assets/{resource_type}/{resource_id}")
	ResponseEntity<JsonNode> deleteAsset(
		@PathVariable("project_id") String projectId,
		@PathVariable("resource_type") Assets.AssetType type,
		@PathVariable("resource_id") String resourceId
	);
}
