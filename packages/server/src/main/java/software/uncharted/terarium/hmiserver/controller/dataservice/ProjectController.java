package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.Id;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionRelationships;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.*;

import java.util.*;

@RequestMapping("/projects")
@RestController
@Slf4j
@RequiredArgsConstructor
@Tags(@Tag(name="Projects", description="Project related operations"))
public class ProjectController {

	final ReBACService reBACService;

	final CurrentUserService currentUserService;

	final ProjectService projectService;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<Project>> getProjects(
		@RequestParam(name = "include_inactive", defaultValue = "false") final Boolean includeInactive
	) {
//		final RebacUser rebacUser = new RebacUser(currentUserService.getToken().getSubject(), reBACService);
//		List<String> projectIds = null;
//		try {
//			projectIds = rebacUser.lookupProjects();
//		} catch (final Exception e) {
//			log.error("Error getting projects which a user can read", e);
//			return ResponseEntity.internalServerError().build();
//		}
//		if (projectIds == null) {
//			return ResponseEntity.noContent().build();
//		}
//
//		// Get projects from the project repository associated with the list of ids. Filter the list of projects to only include active projects.
//		List<Project> projects = projectIds
//			.stream()
//			.map(id -> proxy.getProject(id).getBody())
//			.toList();
//
//		// Remove non-active (soft-deleted) projects
//		projects = projects
//			.stream()
//			.filter(Project::getActive)
//			.toList();
//
//		projects.forEach(project -> {
//			try {
//				final List<AssetType> assetTypes = Arrays.asList(AssetType.datasets, AssetType.models, AssetType.publications);
//
//				final RebacProject rebacProject = new RebacProject(project.getId(), reBACService);
//				project.setPublicProject(rebacProject.isPublic());
//				project.setUserPermission(rebacUser.getPermissionFor(rebacProject));
//
//				final Assets assets = proxy.getAssets(project.getId(), assetTypes).getBody();
//				final Map<String, String> metadata = new HashMap<>();
//				metadata.put("datasets-count", assets.getDatasets() == null ? "0" : String.valueOf(assets.getDatasets().size()));
//				metadata.put("extractions-count", assets.getExtractions() == null ? "0" : String.valueOf(assets.getExtractions().size()));
//				metadata.put("models-count", assets.getModels() == null ? "0" : String.valueOf(assets.getModels().size()));
//				metadata.put("publications-count", assets.getPublications() == null ? "0" : String.valueOf(assets.getPublications().size()));
//				metadata.put("workflows-count", assets.getWorkflows() == null ? "0" : String.valueOf(assets.getWorkflows().size()));
//				metadata.put("artifacts-count", assets.getArtifacts() == null ? "0" : String.valueOf(assets.getArtifacts().size()));
//				project.setMetadata(metadata);
//			} catch (final Exception e) {
//				log.error("Cannot get Datasets, Models, and Publications assets from data-service for project_id {}", project.getId(), e);
//			}
//		});
//
//
//		return ResponseEntity.ok(projects);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Gets the project by id
	 * @param id the UUID for a project
	 * @return The project wrapped in a response entity, a 404 if missing or a 500 if there is a rebac permissions issue.
	 */
	@Operation(summary = "Gets a project by UUID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Project found.",
			content = { @Content(mediaType = "application/json",
				schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Project.class)) }),
		@ApiResponse(responseCode = "500", description = "Error finding project",
			content = @Content),
		@ApiResponse(responseCode = "404", description = "Project not found",
			content = @Content) })
	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Project> getProject(
		@PathVariable("id") final UUID id
	) {
		try {
			final RebacUser rebacUser = new RebacUser(currentUserService.getToken().getSubject(), reBACService);
			final RebacProject rebacProject = new RebacProject(id.toString(), reBACService);
			if (rebacUser.canRead(rebacProject)) {
				final Optional<Project> project = projectService.getProject(id);
				if(project.isPresent()){
					project.get().setPublicProject(rebacProject.isPublic());
					project.get().setUserPermission(rebacUser.getPermissionFor(rebacProject));
					return ResponseEntity.ok(project.get());
				}
			}
			return ResponseEntity.notFound().build();
		} catch (final Exception e) {
			log.error("Error getting project rebac information", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to verify project permissions");
		}
	}

	@GetMapping("/{id}/permissions")
	@Secured(Roles.USER)
	public ResponseEntity<PermissionRelationships> getProjectPermissions(
		@PathVariable("id") final String id
	) {
		try {
			final RebacProject rebacProject = new RebacProject(id, reBACService);
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canRead(rebacProject)) {
				final PermissionRelationships permissions = new PermissionRelationships();
				for (final RebacPermissionRelationship permissionRelationship : rebacProject.getPermissionRelationships()) {
					if (permissionRelationship.getSubjectType().equals(Schema.Type.USER)) {
						permissions.addUser(reBACService.getUser(permissionRelationship.getSubjectId()), permissionRelationship.getRelationship());
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.GROUP)) {
						permissions.addGroup(reBACService.getGroup(permissionRelationship.getSubjectId()), permissionRelationship.getRelationship());
					}
				}

				return ResponseEntity.ok(permissions);
			}
			return ResponseEntity.notFound().build();
		} catch (final Exception e) {
			log.error("Error getting project permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}


	@PostMapping("/{projectId}/permissions/group/{groupId}/{relationship}")
	@Secured({Roles.USER, Roles.SERVICE})
	public ResponseEntity<JsonNode> setProjectGroupPermissions(
		@PathVariable("projectId") final String projectId,
		@PathVariable("groupId") final String groupId,
		@PathVariable("relationship") final String relationship
	) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacGroup who = new RebacGroup(groupId, reBACService);
			return setProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error setting project group permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PutMapping("/{projectId}/permissions/group/{groupId}/{oldRelationship}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> updateProjectGroupPermissions(
		@PathVariable("projectId") final String projectId,
		@PathVariable("groupId") final String groupId,
		@PathVariable("oldRelationship") final String oldRelationship,
		@RequestParam("to") final String newRelationship
	) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacGroup who = new RebacGroup(groupId, reBACService);
			return updateProjectPermissions(what, who, oldRelationship, newRelationship);
		} catch (final Exception e) {
			log.error("Error deleting project user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/{projectId}/permissions/group/{groupId}/{relationship}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> removeProjectGroupPermissions(
		@PathVariable("projectId") final String projectId,
		@PathVariable("groupId") final String groupId,
		@PathVariable("relationship") final String relationship
	) {
		if (relationship.equalsIgnoreCase(Schema.Relationship.CREATOR.toString())) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacGroup who = new RebacGroup(groupId, reBACService);
			return removeProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error deleting project group permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/{projectId}/permissions/user/{userId}/{relationship}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> setProjectUserPermissions(
		@PathVariable("projectId") final String projectId,
		@PathVariable("userId") final String userId,
		@PathVariable("relationship") final String relationship
	) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacUser who = new RebacUser(userId, reBACService);
			return setProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error setting project user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PutMapping("/{projectId}/permissions/user/{userId}/{oldRelationship}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> updateProjectUserPermissions(
		@PathVariable("projectId") final String projectId,
		@PathVariable("userId") final String userId,
		@PathVariable("oldRelationship") final String oldRelationship,
		@RequestParam("to") final String newRelationship
	) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacUser who = new RebacUser(userId, reBACService);
			return updateProjectPermissions(what, who, oldRelationship, newRelationship);
		} catch (final Exception e) {
			log.error("Error deleting project user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/{projectId}/permissions/user/{userId}/{relationship}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> removeProjectUserPermissions(
		@PathVariable("projectId") final String projectId,
		@PathVariable("userId") final String userId,
		@PathVariable("relationship") final String relationship
	) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacUser who = new RebacUser(userId, reBACService);
			return removeProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error deleting project user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	private ResponseEntity<JsonNode> setProjectPermissions(final RebacProject what, final RebacObject who, final String relationship) throws Exception {
		if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(what)) {
			try {
				what.setPermissionRelationships(who, relationship);
				return ResponseEntity.ok().build();
			} catch (final RelationshipAlreadyExistsException e) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}
		}
		return ResponseEntity.notFound().build();
	}

	private ResponseEntity<JsonNode> updateProjectPermissions(final RebacProject what, final RebacObject who, final String oldRelationship, final String newRelationship) throws Exception {
		if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(what)) {
			try {
				what.removePermissionRelationships(who, oldRelationship);
				what.setPermissionRelationships(who, newRelationship);
				return ResponseEntity.ok().build();
			} catch (final RelationshipAlreadyExistsException e) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}
		}
		return ResponseEntity.notFound().build();
	}

	private ResponseEntity<JsonNode> removeProjectPermissions(final RebacProject what, final RebacObject who, final String relationship) throws Exception {
		if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(what)) {
			try {
				what.removePermissionRelationships(who, relationship);
				return ResponseEntity.ok().build();
			} catch (final RelationshipAlreadyExistsException e) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}
		}
		return ResponseEntity.notFound().build();
	}


	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<Project> createProject(
		@RequestBody Project project
	) {

		project = projectService.save(Project.cloneFrom(project));

		try {
			new RebacUser(currentUserService.getToken().getSubject(), reBACService).createCreatorRelationship(new RebacProject(project.getId().toString(), reBACService));
		} catch (final Exception e) {
			log.error("Error setting user's permissions for project", e);
			// TODO: Rollback potential?
		} catch (final RelationshipAlreadyExistsException e) {
			log.error("Error the user is already the creator of this project", e);
			// TODO: Rollback potential?
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(project);

	}

	@PutMapping("/{project_id}")
	@Secured(Roles.USER)
	public ResponseEntity<Project> updateProject(
		@PathVariable("project_id") final UUID id,
		@RequestBody final Project project
	) {
		try {
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canWrite(new RebacProject(id.toString(), reBACService))) {
				project.setId(id);
				return ResponseEntity.ok(projectService.save(project));
			}
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		} catch (final Exception e) {
			log.error("Error updating project", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> deleteProject(
		@PathVariable("id") final String id
	) {

//		try {
//			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(new RebacProject(id, reBACService))) {
//				return ResponseEntity.ok(proxy.deleteProject(id).getBody());
//			}
//			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
//		} catch (final Exception e) {
//			log.error("Error deleting project", e);
//			return ResponseEntity.internalServerError().build();
//		}
		return ResponseEntity.noContent().build();

	}

	@GetMapping("/{project_id}/assets")
	@Secured(Roles.USER)
	public ResponseEntity<Assets> getAssets(
		@PathVariable("project_id") final String projectId,
		@RequestParam("types") final List<AssetType> types
	) {
//		try {
//			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canRead(new RebacProject(projectId, reBACService))) {
//				return ResponseEntity.ok(proxy.getAssets(projectId, types).getBody());
//			}
//			return ResponseEntity.notFound().build();
//		} catch (final Exception e) {
//			log.error("Error getting project assets", e);
//			return ResponseEntity.internalServerError().build();
//		}
		return ResponseEntity.noContent().build();


	}

	@PostMapping("/{project_id}/assets/{resource_type}/{resource_id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> createAsset(
		@PathVariable("project_id") final String projectId,
		@PathVariable("resource_type") final AssetType type,
		@PathVariable("resource_id") final String resourceId
	) {


//		try {
//			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canWrite(new RebacProject(projectId, reBACService))) {
//				return ResponseEntity.ok(proxy.createAsset(projectId, type, resourceId).getBody());
//			}
//			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
//		} catch (final Exception e) {
//			log.error("Error creating project assets", e);
//			return ResponseEntity.internalServerError().build();
//		}

		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{project_id}/assets/{resource_type}/{resource_id}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> deleteAsset(
		@PathVariable("project_id") final String projectId,
		@PathVariable("resource_type") final AssetType type,
		@PathVariable("resource_id") final String resourceId
	) {

//		try {
//			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canWrite(new RebacProject(projectId, reBACService))) {
//				return ResponseEntity.ok(proxy.deleteAsset(projectId, type, resourceId).getBody());
//			}
//			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
//		} catch (final Exception e) {
//			log.error("Error deleting project assets", e);
//			return ResponseEntity.internalServerError().build();
//		}
		return ResponseEntity.noContent().build();
	}
}
