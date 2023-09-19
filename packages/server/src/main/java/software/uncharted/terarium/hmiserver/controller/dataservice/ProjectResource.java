package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;

import java.util.*;

@RequestMapping("/projects")
@RestController
@Slf4j
public class ProjectResource {

	@Autowired
	private ProjectProxy proxy;

	@GetMapping
	public ResponseEntity<List<Project>> getProjects(
		@RequestParam(name = "include_inactive", defaultValue="false") final Boolean includeInactive
	) {
		List<Project> projects = proxy.getProjects(includeInactive).getBody();
		if(projects == null) {
			return ResponseEntity.noContent().build();
		}

		// Remove non-active (soft-deleted) projects

		projects = projects
			.stream()
			.filter(Project::getActive)
			.toList();

		projects.forEach(project -> {
			try {
				List<AssetType> assetTypes= Arrays.asList(AssetType.datasets, AssetType.models, AssetType.publications);

				Assets assets = proxy.getAssets(project.getProjectID(), assetTypes).getBody();
				Map<String, String> metadata = new HashMap<>();
				metadata.put("datasets-count", assets.getDatasets() == null ? "0" : String.valueOf(assets.getDatasets().size()));
				metadata.put("extractions-count", assets.getExtractions() == null ? "0" : String.valueOf(assets.getExtractions().size()));
				metadata.put("models-count", assets.getModels() == null ? "0" : String.valueOf(assets.getModels().size()));
				metadata.put("publications-count", assets.getPublications() == null ? "0" : String.valueOf(assets.getPublications().size()));
				metadata.put("workflows-count", assets.getWorkflows() == null ? "0" : String.valueOf(assets.getWorkflows().size()));
				metadata.put("artifacts-count", assets.getArtifacts() == null ? "0" : String.valueOf(assets.getArtifacts().size()));
				project.setMetadata(metadata);
			} catch (Exception e) {
				log.error("Cannot get Datasets, Models, and Publications assets from data-service for project_id {}", project.getProjectID(), e);
			}
		});



		return ResponseEntity.ok(projects);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Project> getProject(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(proxy.getProject(id).getBody());
	}

	@PostMapping
	public ResponseEntity<JsonNode> createProject(
		@RequestBody final Project project
	) {
		return ResponseEntity.ok(proxy.createProject(project).getBody());
	}

	@PutMapping("/{id}")
	public ResponseEntity<JsonNode> updateProject(
		@PathVariable("id") final String id,
		final Project project
	) {
		return ResponseEntity.ok(proxy.updateProject(id, project).getBody());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<JsonNode> deleteProject(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(proxy.deleteProject(id).getBody());
	}

	@GetMapping("/{project_id}/assets")
	public ResponseEntity<Assets> getAssets(
		@PathVariable("project_id") final String projectId,
		@RequestParam("types") final List<AssetType> types
	) {
		return ResponseEntity.ok(proxy.getAssets(projectId, types).getBody());

	}

	@PostMapping("/{project_id}/assets/{resource_type}/{resource_id}")
	public ResponseEntity<JsonNode> createAsset(
		@PathVariable("project_id") final String projectId,
		@PathVariable("resource_type") final AssetType type,
		@PathVariable("resource_id") final String resourceId
	) {
		return ResponseEntity.ok(proxy.createAsset(projectId, type, resourceId).getBody());
	}

	@DeleteMapping("/{project_id}/assets/{resource_type}/{resource_id}")
	public ResponseEntity<JsonNode> deleteAsset(
		@PathVariable("project_id") final String projectId,
		@PathVariable("resource_type") final AssetType type,
		@PathVariable("resource_id") final String resourceId
	) {
		return ResponseEntity.ok(proxy.deleteAsset(projectId, type, resourceId).getBody());
	}
}
