package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionRelationships;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ITerariumAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.TerariumAssetServices;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacObject;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacPermissionRelationship;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacProject;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

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

	final TerariumAssetServices terariumAssetServices;

	final ObjectMapper objectMapper;

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
			@RequestParam(name = "include-inactive", defaultValue = "false") final Boolean includeInactive) {
		final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
		List<UUID> projectIds = null;
		try {
			projectIds = rebacUser.lookupProjects();
		} catch (final Exception e) {
			log.error("Error getting projects which a user can read", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Error getting projects which a user can read");
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

				final List<Contributor> contributors = getContributors(rebacProject);

				final List<ProjectAsset> assets = projectAssetService.findActiveAssetsForProject(project.getId(),
						assetTypes);

				final Map<String, String> metadata = new HashMap<>();

				final Map<AssetType, Integer> counts = new HashMap<>();
				for (final ProjectAsset asset : assets) {
					counts.put(asset.getAssetType(), counts.getOrDefault(asset.getAssetType(), 0) + 1);
				}

				metadata.put("contributor-count", Integer.toString(contributors.size()));
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
	 * A Contributor is a User or Group that is capable of editing a Project.
	 */
	private class Contributor {
		String name;
		Schema.Relationship permission;

		Contributor(final String name, final Schema.Relationship permission) {
			this.name = name;
			this.permission = permission;
		}
	}

	/**
	 * Capture the subset of RebacPermissionRelationships for a given Project.
	 *
	 * @param rebacProject the Project to collect RebacPermissionRelationships of.
	 * @return List of Users and Groups who have edit capability of the rebacProject
	 */
	private List<Contributor> getContributors(final RebacProject rebacProject) {
		final Map<String, Contributor> contributorMap = new HashMap<>();

		try {
			final List<RebacPermissionRelationship> permissionRelationships = rebacProject.getPermissionRelationships();
			for (final RebacPermissionRelationship permissionRelationship : permissionRelationships) {
				final Schema.Relationship relationship = permissionRelationship.getRelationship();
				// Ensure the relationship is capable of editing the project
				if (relationship.equals(Schema.Relationship.CREATOR)
						|| relationship.equals(Schema.Relationship.ADMIN)
						|| relationship.equals(Schema.Relationship.WRITER)) {
					if (permissionRelationship.getSubjectType().equals(Schema.Type.USER)) {
						final PermissionUser user = reBACService.getUser(permissionRelationship.getSubjectId());
						final String name = user.getFirstName() + " " + user.getLastName();
						if (!contributorMap.containsKey(name)) {
							contributorMap.put(name, new Contributor(name, relationship));
						}
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.GROUP)) {
						final PermissionGroup group = reBACService.getGroup(permissionRelationship.getSubjectId());
						if (!contributorMap.containsKey(group.getName())) {
							contributorMap.put(group.getName(), new Contributor(group.getName(), relationship));
						}
					}
				}
			}
		} catch (final Exception e) {
			log.error("Failed to get project's contributors");
		}

		return new ArrayList<>(contributorMap.values());
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
					final List<String> authors = new ArrayList<>();
					final List<Contributor> contributors = getContributors(rebacProject);
					for (final Contributor contributor : contributors) {
						authors.add(contributor.name);
					}

					project.get().setPublicProject(rebacProject.isPublic());
					project.get().setUserPermission(rebacUser.getPermissionFor(rebacProject));
					project.get().setAuthors(authors);
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
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(id, reBACService);
			if (rebacUser.canAdministrate(rebacProject)) {
				final boolean deleted = projectService.delete(id);
				if (deleted)
					return ResponseEntity.ok(new ResponseDeleted("project", id));
			}
		} catch (final Exception e) {
			log.error("Error deleting project", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete project");
		}

		throw new ResponseStatusException(
				HttpStatus.NOT_MODIFIED,
				"Unable to delete project, please try again later.");
	}

	@Operation(summary = "Creates a new project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Project created", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Project.class)), }),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving sessions from the data store", content = @Content) })
	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<Project> createProject(
			@RequestBody Project project) {
		if (project.getOverviewContent() == null) {
			final String welcomeMessage = """
					<div>
						<h2>Hey there!</h2>
						<p>This is your project overview page. Use this space however you like. Not sure where to start? Here are some things you can try:</p>
						<br>
							<ul>
								<li><strong>Upload stuff:</strong> Upload documents, models, code or datasets with the green button in the bottom left corner.</li>
								<li><strong>Explore and add:</strong> Use the project selector in the top nav to switch to the Explorer where you can find documents, models and datasets that you can add to your project.</li>
								<li><strong>Build a model:</strong> Create a model that fits just what you need.</li>
								<li><strong>Create a workflow:</strong> Connect resources with operators so you can focus on the science and not the plumbing.</li>
							</ul>
						<br>
						<p>Feel free to erase this text and make it your own.</p>
					</div>
					""";
			project.setOverviewContent(welcomeMessage.getBytes());
		}
		project = projectService.createProject(project);

		try {
			final RebacProject rebacProject = new RebacProject(project.getId(), reBACService);
			final RebacGroup rebacAskemAdminGroup = new RebacGroup(ReBACService.ASKEM_ADMIN_GROUP_ID, reBACService);
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);

			rebacUser.createCreatorRelationship(rebacProject);
			rebacAskemAdminGroup.createWriterRelationship(rebacProject);
		} catch (final Exception e) {
			log.error("Error setting user's permissions for project", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Error setting user's permissions for project");
		} catch (final RelationshipAlreadyExistsException e) {
			log.error("Error the user is already the creator of this project", e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
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
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(id, reBACService);
			if (rebacUser.canWrite(rebacProject)) {
				project.setId(id);
				final Optional<Project> updatedProject = projectService.updateProject(project);
				return updatedProject.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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

	@Operation(summary = "Creates an asset inside of a given project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Asset Created", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProjectAsset.class)) }),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "409", description = "Asset already exists in this project", content = @Content),
			@ApiResponse(responseCode = "500", description = "Error finding project", content = @Content) })
	@PostMapping("/{id}/assets/{asset-type}/{asset-id}")
	@Secured(Roles.USER)
	public ResponseEntity<ProjectAsset> createAsset(
			@PathVariable("id") final UUID projectId,
			@PathVariable("asset-type") final String assetTypeName,
			@PathVariable("asset-id") final UUID assetId) {

		final AssetType assetType = AssetType.getAssetType(assetTypeName, objectMapper);

		try {
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(projectId, reBACService);
			if (rebacUser.canWrite(rebacProject)) {
				final Optional<Project> project = projectService.getProject(projectId);

				if (project.isPresent()) {

					// double check that this asset is not already a part of this project, and if it
					// does exist return a 409 to the front end
					final Optional<ProjectAsset> existingAsset = projectAssetService
							.getProjectAssetByProjectIdAndAssetId(projectId, assetId);
					if (existingAsset.isPresent()) {
						return ResponseEntity.status(HttpStatus.CONFLICT).body(existingAsset.get());
					}

					final ITerariumAssetService<? extends TerariumAsset> terariumAssetService = terariumAssetServices
							.getServiceByType(assetType);
					final Optional<? extends TerariumAsset> asset = terariumAssetService.getAsset(assetId);
					if (asset.isPresent()) {
						final Optional<ProjectAsset> projectAsset = projectAssetService.createProjectAsset(project.get(), assetType,
								asset.get());
						return projectAsset.map(pa -> ResponseEntity.status(HttpStatus.CREATED).body(pa))
								.orElseGet(() -> ResponseEntity.notFound().build());
					} else {
						return ResponseEntity.notFound().build();
					}
				}
			}
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
	@DeleteMapping("/{id}/assets/{asset-type}/{asset-id}")
	@Secured(Roles.USER)
	public ResponseEntity<ResponseDeleted> deleteAsset(
			@PathVariable("id") final UUID projectId,
			@PathVariable("asset-type") final String assetTypeName,
			@PathVariable("asset-id") final UUID assetId) {

		final AssetType assetType = AssetType.getAssetType(assetTypeName, objectMapper);

		try {
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(projectId, reBACService);
			if (rebacUser.canWrite(rebacProject)) {
				final boolean deleted = projectAssetService.deleteByAssetId(projectId, assetType, assetId);
				if (deleted) {
					return ResponseEntity.ok(new ResponseDeleted("ProjectAsset " + assetTypeName, assetId));
				}
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
			final RebacUser rebacUser = new RebacUser(currentUserService.get().getId(), reBACService);
			final RebacProject rebacProject = new RebacProject(id, reBACService);
			if (rebacUser.canRead(rebacProject)) {
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

	@PostMapping("/{id}/permissions/group/{group-id}/{relationship}")
	@Secured({ Roles.USER, Roles.SERVICE })
	@Operation(summary = "Sets a group's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions set", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> setProjectGroupPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("group-id") final String groupId,
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

	@DeleteMapping("/{id}/permissions/group/{group-id}/{relationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a group's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions deleted", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> removeProjectGroupPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("group-id") final String groupId,
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

	@Operation(summary = "Toggle a project public, or restricted, by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Project visibility has been updated", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UUID.class)) }),
			@ApiResponse(responseCode = "304", description = "The current user does not have privileges to modify this project.", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	@PutMapping("/set-public/{id}/{isPublic}")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> makeProjectPublic(
			@PathVariable("id") final UUID id,
			@PathVariable("isPublic") final boolean isPublic) {
		try {
			// Getting the project permissions
			final RebacProject project = new RebacProject(id, reBACService);
			// Getting the user permissions
			final RebacUser user = new RebacUser(currentUserService.get().getId(), reBACService);
			// Getting the Public group permissions
			final RebacGroup who = new RebacGroup(ReBACService.PUBLIC_GROUP_ID, reBACService);
			// Setting the relationship to be of a reader
			final String relationship = Schema.Relationship.READER.toString();

			// If the current user is an admin of the project
			if (user.canAdministrate(project)) {
				if (isPublic) {
					// Set the Public Group permissions to READ the Project
					return setProjectPermissions(project, who, relationship);
				} else {
					// Remove the Public Group permissions to READ the Project
					return removeProjectPermissions(project, who, relationship);
				}
			} else {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}
		} catch (final Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/{id}/permissions/user/{user-id}/{relationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Sets a user's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions set", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> setProjectUserPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("user-id") final String userId,
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

	@PutMapping("/{id}/permissions/user/{user-id}/{old-relationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Updates a user's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions updated", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> updateProjectUserPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("user-id") final String userId,
			@PathVariable("old-relationship") final String oldRelationship,
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

	@DeleteMapping("/{id}/permissions/user/{user-id}/{relationship}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a user's permissions for a project")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions deleted", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PermissionRelationships.class))),
			@ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred verifying permissions", content = @Content) })
	public ResponseEntity<JsonNode> removeProjectUserPermissions(
			@PathVariable("id") final UUID projectId,
			@PathVariable("user-id") final String userId,
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
