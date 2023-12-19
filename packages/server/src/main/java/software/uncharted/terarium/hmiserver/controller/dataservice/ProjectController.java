package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionRelationships;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
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
@Transactional
@Tags(@Tag(name = "Projects", description = "Project related operations"))
public class ProjectController {

	final ReBACService reBACService;

	final CurrentUserService currentUserService;

	final ProjectService projectService;

	final ProjectAssetService projectAssetService;

	// --------------------------------------------------------------------------
	// Basic Project Operations
	// --------------------------------------------------------------------------

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all projects (which are visible to this user)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Projects found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Project.class)))),
			@ApiResponse(responseCode = "204", description = "There are no errors, but also no projects for this user", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue with rebac permissions", content = @Content) })
	public ResponseEntity<List<Project>> getProjects(
			@RequestParam(name = "include_inactive", defaultValue = "false") final Boolean includeInactive) {
		final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
		List<UUID> projectIds = null;
		try {
			projectIds = rebacUser.lookupProjects();
		} catch (final Exception e) {
			log.error("Error getting projects which a user can read", e);
			return ResponseEntity.internalServerError().build();
		}
		if (projectIds == null || projectIds.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		// Get projects from the project repository associated with the list of ids.
		// Filter the list of projects to only include active projects.
		final List<Project> projects = includeInactive ? projectService.getProjects(projectIds)
				: projectService.getActiveProjects(projectIds);

		projects.forEach(project -> {
			try {
				final List<AssetType> assetTypes = Arrays.asList(
						AssetType.DATASET,
						AssetType.MODEL,
						AssetType.DOCUMENT,
						AssetType.WORKFLOW,
						AssetType.PUBLICATION);

				final RebacProject rebacProject = new RebacProject(project.getId(), reBACService);
				project.setPublicProject(rebacProject.isPublic());
				project.setUserPermission(rebacUser.getPermissionFor(rebacProject));

				final List<ProjectAsset> assets = projectAssetService.findActiveAssetsForProject(project.getId(),
						assetTypes);

				final Map<String, String> metadata = new HashMap<>();

				final Map<AssetType, Integer> counts = new HashMap<>();
				for (final ProjectAsset asset : assets) {
					counts.put(asset.getAssetType(), counts.getOrDefault(asset.getAssetType(), 0) + 1);
				}

				metadata.put("datasets-count", counts.getOrDefault(AssetType.DATASET, 0).toString());
				metadata.put("document-count", counts.getOrDefault(AssetType.DOCUMENT, 0).toString());
				metadata.put("models-count", counts.getOrDefault(AssetType.MODEL, 0).toString());
				metadata.put("workflows-count", counts.getOrDefault(AssetType.WORKFLOW, 0).toString());
				metadata.put("publications-count", counts.getOrDefault(AssetType.PUBLICATION, 0).toString());

				project.setMetadata(metadata);
			} catch (final Exception e) {
				log.error("Cannot get Datasets, Models, and Publications assets from data-service for project_id {}",
						project.getId(), e);
			}
		});

		return ResponseEntity.ok(projects);
	}

	/**
	 * Gets the project by id
	 *
	 * @param id the UUID for a project
	 * @return The project wrapped in a response entity, a 404 if missing or a 500
	 *         if there is a rebac permissions issue.
	 */
	@Operation(summary = "Gets a project by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Project found.", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Project.class)) }),
			@ApiResponse(responseCode = "500", description = "Error finding project", content = @Content),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content) })
	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Project> getProject(
			@PathVariable("id") final UUID id) {
		try {
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(id, reBACService);
			if (rebacUser.canRead(rebacProject)) {
				final Optional<Project> project = projectService.getProject(id);
				if (project.isPresent()) {
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

	@Operation(summary = "Soft deletes project by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Project marked for deletion", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UUID.class)) }),
			@ApiResponse(responseCode = "404", description = "Project could not be found", content = @Content),
			@ApiResponse(responseCode = "304", description = "The current user does not have delete privileges to this project", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseDeleted> deleteProject(
			@PathVariable("id") final UUID id) {

		try {
			if (new RebacUser(currentUserService.get().getId(), reBACService)
					.canAdministrate(new RebacProject(id, reBACService))) {
				final boolean deleted = projectService.delete(id);
				if (deleted)
					return ResponseEntity.ok(new ResponseDeleted("project", id));
			}

			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.NOT_MODIFIED,
					"Failed to delete project");

		} catch (final Exception e) {
			log.error("Error deleting project", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete project");
		}

	}

	@Operation(summary = "Creates a new project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Project created", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Project.class)), }),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving sessions from the data store", content = @Content)}
	)
	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<Project> createProject(
			@RequestBody Project project) {

		project = projectService.createProject(project);

		try {
			new RebacUser(currentUserService.get().getId(), reBACService)
					.createCreatorRelationship(new RebacProject(project.getId(), reBACService));
		} catch (final Exception e) {
			log.error("Error setting user's permissions for project", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Error setting user's permissions for project");
		} catch (final RelationshipAlreadyExistsException e) {
			log.error("Error the user is already the creator of this project", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Error the user is already the creator of this project");
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(project);

	}

	@Operation(summary = "Updates a project by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Project marked for deletion", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UUID.class)) }),
			@ApiResponse(responseCode = "404", description = "Project could not be found", content = @Content),
			@ApiResponse(responseCode = "304", description = "The current user does not have delete privileges to this project", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Project> updateProject(
			@PathVariable("id") final UUID id,
			@RequestBody final Project project) {
		try {
			if (new RebacUser(currentUserService.get().getId(), reBACService)
					.canWrite(new RebacProject(id, reBACService))) {

				final Optional<Project> updatedProject = projectService.updateProject(project.setId(id));
				if (updatedProject.isEmpty()) {
					return ResponseEntity.notFound().build();
				}
				return ResponseEntity.ok(updatedProject.get());
			}
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		} catch (final Exception e) {
			log.error("Error updating project", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update project");
		}
	}

	// --------------------------------------------------------------------------
	// Project Assets
	// --------------------------------------------------------------------------

	@Operation(summary = "Gets the assets belonging to a specific project, by asset type")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Assets found", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProjectAsset.class)))),
			@ApiResponse(responseCode = "204", description = "Currently unimplemented. This is all you'll get for now!", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProjectAsset.class)))),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	@GetMapping("/{id}/assets")
	@Secured(Roles.USER)
	public ResponseEntity<List<ProjectAsset>> getAssets(
			@PathVariable("id") final UUID projectId,
			@RequestParam("types") final List<AssetType> types) {
		try {
			if (new RebacUser(currentUserService.get().getId(), reBACService)
					.canRead(new RebacProject(projectId, reBACService))) {

				final List<ProjectAsset> assets = projectAssetService.findActiveAssetsForProject(projectId, types);

				return ResponseEntity.ok(assets);
			}
			return ResponseEntity.notFound().build();
		} catch (final Exception e) {
			log.error("Error getting project assets", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to get project assets");
		}

	}

	@Operation(summary = "Creates an asset inside of a given project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Asset Created", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProjectAsset.class)) }),
			@ApiResponse(responseCode = "500", description = "Error finding project", content = @Content) })
	@PostMapping("/{id}/assets/{asset_type}/{asset_id}")
	@Secured(Roles.USER)
	public ResponseEntity<ProjectAsset> createAsset(
			@PathVariable("id") final UUID projectId,
			@PathVariable("asset_type") final AssetType type,
			@PathVariable("asset_id") final UUID assetId) {

		try {
			if (new RebacUser(currentUserService.get().getId(), reBACService)
					.canWrite(new RebacProject(projectId, reBACService))) {
				final Optional<Project> project = projectService.getProject(projectId);
				if (project.isPresent()) {
					final ProjectAsset asset = projectAssetService.createProjectAsset(project.get(), type, assetId);
					return ResponseEntity.status(HttpStatus.CREATED).body(asset);
				}
			}
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		} catch (final Exception e) {
			log.error("Error creating project assets", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to create project asset");
		}
	}

	@Operation(summary = "Deletes an asset inside of a given project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Asset Deleted", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UUID.class)) }),
			@ApiResponse(responseCode = "204", description = "User may not have permission to this project", content = @Content),
			@ApiResponse(responseCode = "500", description = "Error finding project", content = @Content) })
	@DeleteMapping("/{id}/assets/{asset_type}/{asset_id}")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseDeleted> deleteAsset(
			@PathVariable("id") final UUID projectId,
			@PathVariable("asset_type") final AssetType type,
			@PathVariable("asset_id") final UUID assetId) {

		try {
			if (new RebacUser(currentUserService.get().getId(), reBACService)
					.canWrite(new RebacProject(projectId, reBACService))) {
				final boolean deleted = projectAssetService.delete(assetId);
				if (deleted)
					return ResponseEntity.ok(new ResponseDeleted("asset-" + type, assetId));
			}
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		} catch (final Exception e) {
			log.error("Error deleting project assets", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete project asset");
		}
	}

	// --------------------------------------------------------------------------
	// Project Permissions
	// --------------------------------------------------------------------------

	@GetMapping("/{id}/permissions")
	@Secured(Roles.USER)
	@Operation(summary = "Gets the permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions found", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<PermissionRelationships> getProjectPermissions(
			@PathVariable("id") final UUID id) {
		try {
			final RebacProject rebacProject = new RebacProject(id, reBACService);
			if (new RebacUser(currentUserService.get().getId(), reBACService).canRead(rebacProject)) {
				final PermissionRelationships permissions = new PermissionRelationships();
				for (final RebacPermissionRelationship permissionRelationship : rebacProject
						.getPermissionRelationships()) {
					if (permissionRelationship.getSubjectType().equals(Schema.Type.USER)) {
						permissions.addUser(reBACService.getUser(permissionRelationship.getSubjectId()),
								permissionRelationship.getRelationship());
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.GROUP)) {
						permissions.addGroup(reBACService.getGroup(permissionRelationship.getSubjectId()),
								permissionRelationship.getRelationship());
					}
				}

				return ResponseEntity.ok(permissions);
			}
			return ResponseEntity.notFound().build();
		} catch (final Exception e) {
			log.error("Error getting project permission relationships", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error getting project permission relationships");
		}
	}

	@PostMapping("/{id}/permissions/group/{groupId}/{relationship}")
	@Secured({ Roles.USER, Roles.SERVICE })
	@Operation(summary = "Sets a group's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions set", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> setProjectGroupPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("groupId") final String groupId,
			@PathVariable("relationship") final String relationship) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacGroup who = new RebacGroup(groupId, reBACService);
			return setProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error setting project group permission relationships", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error setting project group permission relationships");
		}
	}

	@PutMapping("/{id}/permissions/group/{groupId}/{oldRelationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Updates a group's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions updated", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> updateProjectGroupPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("groupId") final String groupId,
			@PathVariable("oldRelationship") final String oldRelationship,
			@RequestParam("to") final String newRelationship) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacGroup who = new RebacGroup(groupId, reBACService);
			return updateProjectPermissions(what, who, oldRelationship, newRelationship);
		} catch (final Exception e) {
			log.error("Error deleting project user permission relationships", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error deleting project user permission relationships");
		}
	}

	@DeleteMapping("/{id}/permissions/group/{groupId}/{relationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a group's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions deleted", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> removeProjectGroupPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("groupId") final String groupId,
			@PathVariable("relationship") final String relationship) {
		if (relationship.equalsIgnoreCase(Schema.Relationship.CREATOR.toString())) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacGroup who = new RebacGroup(groupId, reBACService);
			return removeProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error deleting project group permission relationships", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error deleting project group permission relationships");
		}
	}

	@PostMapping("/{id}/permissions/user/{userId}/{relationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Sets a user's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions set", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> setProjectUserPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("userId") final String userId,
			@PathVariable("relationship") final String relationship) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacUser who = new RebacUser(userId, reBACService);
			return setProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error setting project user permission relationships", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error setting project user permission relationships");
		}
	}

	@PutMapping("/{id}/permissions/user/{userId}/{oldRelationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Updates a user's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions updated", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> updateProjectUserPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("userId") final String userId,
			@PathVariable("oldRelationship") final String oldRelationship,
			@RequestParam("to") final String newRelationship) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacUser who = new RebacUser(userId, reBACService);
			return updateProjectPermissions(what, who, oldRelationship, newRelationship);
		} catch (final Exception e) {
			log.error("Error deleting project user permission relationships", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error deleting project user permission relationships");
		}
	}

	@DeleteMapping("/{id}/permissions/user/{userId}/{relationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a user's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions deleted", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> removeProjectUserPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("userId") final String userId,
			@PathVariable("relationship") final String relationship) {
		try {
			final RebacProject what = new RebacProject(projectId, reBACService);
			final RebacUser who = new RebacUser(userId, reBACService);
			return removeProjectPermissions(what, who, relationship);
		} catch (final Exception e) {
			log.error("Error deleting project user permission relationships", e);
			throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Error deleting project user permission relationships");
		}
	}

	private ResponseEntity<JsonNode> setProjectPermissions(final RebacProject what, final RebacObject who,
			final String relationship) throws Exception {
		if (new RebacUser(currentUserService.get().getId(), reBACService).canAdministrate(what)) {
			try {
				what.setPermissionRelationships(who, relationship);
				return ResponseEntity.ok().build();
			} catch (final RelationshipAlreadyExistsException e) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}
		}
		return ResponseEntity.notFound().build();
	}

	private ResponseEntity<JsonNode> updateProjectPermissions(final RebacProject what, final RebacObject who,
			final String oldRelationship, final String newRelationship) throws Exception {
		if (new RebacUser(currentUserService.get().getId(), reBACService).canAdministrate(what)) {
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

	private ResponseEntity<JsonNode> removeProjectPermissions(final RebacProject what, final RebacObject who,
			final String relationship) throws Exception {
		if (new RebacUser(currentUserService.get().getId(), reBACService).canAdministrate(what)) {
			try {
				what.removePermissionRelationships(who, relationship);
				return ResponseEntity.ok().build();
			} catch (final RelationshipAlreadyExistsException e) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}
		}
		return ResponseEntity.notFound().build();
	}

}
