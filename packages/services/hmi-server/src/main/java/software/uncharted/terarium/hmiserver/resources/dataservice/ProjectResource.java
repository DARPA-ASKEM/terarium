package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.jwt.JsonWebToken;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.Id;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;
import software.uncharted.terarium.hmiserver.models.dataservice.permission.PermissionRelationships;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Project REST Endpoints")
@Slf4j
public class ProjectResource {
	@Inject
	ReBACService reBACService;
	@Inject
	JsonWebToken jwt;

	@Inject
	@RestClient
	ProjectProxy proxy;

	@GET
	public Response getProjects(
		@DefaultValue("250") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		List<Project> projects = proxy.getProjects(pageSize, page);

		// Remove non-active (soft-deleted) projects
		projects = projects
			.stream()
			.filter(Project::getActive)
			.filter(project -> {
				try {
					return new RebacUser(jwt.getSubject(), reBACService).canRead(new RebacProject(project.getProjectID(), reBACService));
				} catch (Exception e) {
					log.error("Error getting user's permissions for project", e);
				}
			})
			.toList();

		projects.forEach(project -> {
			try {
				Assets assets = proxy.getAssets(project.getProjectID(), Arrays.asList(Assets.AssetType.DATASETS, Assets.AssetType.MODELS, Assets.AssetType.PUBLICATIONS));
				Map<String, String> metadata = new HashMap<>();
				metadata.put("datasets-count", assets.getDatasets() == null ? "0" : String.valueOf(assets.getDatasets().size()));
				metadata.put("extractions-count", assets.getExtractions() == null ? "0" : String.valueOf(assets.getExtractions().size()));
				metadata.put("models-count", assets.getModels() == null ? "0" : String.valueOf(assets.getModels().size()));
				metadata.put("publications-count", assets.getPublications() == null ? "0" : String.valueOf(assets.getPublications().size()));
				metadata.put("workflows-count", assets.getWorkflows() == null ? "0" : String.valueOf(assets.getWorkflows().size()));
				metadata.put("artifacts-count", assets.getArtifacts() == null ? "0" : String.valueOf(assets.getArtifacts().size()));
				project.setMetadata(metadata);
			} catch (Exception e) {
				log.info("Cannot get Datasets, Models, and Publications assets from data-service for project_id {}", project.getProjectID());
			}
		});

		return Response
			.status(Response.Status.OK)
			.entity(projects)
			.build();
	}

	@GET
	@Path("/{id}")
	public Response getProject(
		@PathParam("id") final String id
	) {
		try {
			RebacProject rebacProject = new RebacProject(id, reBACService);
			if (new RebacUser(jwt.getSubject(), reBACService).canRead(rebacProject)) {
				return proxy.getProject(id);
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("Error getting project", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/{id}/permissions")
	public Response getProjectPermissions(
		@PathParam("id") final String id
	) {
		try {
			RebacProject rebacProject = new RebacProject(id, reBACService);
			if (new RebacUser(jwt.getSubject(), reBACService).canRead(rebacProject)) {
				PermissionRelationships permissions = new PermissionRelationships();
				for (RebacPermissionRelationship permissionRelationship : rebacProject.getPermissionRelationships()) {
					if (permissionRelationship.getSubjectType().equals(Schema.Type.USER)) {
						permissions.addUser(permissionRelationship.getSubjectId(), permissionRelationship.getRelationship());
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.GROUP)) {
						permissions.addGroup(permissionRelationship.getSubjectId(), permissionRelationship.getRelationship());
					}
				}

				return Response
					.status(Response.Status.OK)
					.entity(permissions)
					.build();
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("Error getting project permission relationships", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("/{projectId}/permissions/group/{groupId}/{relationship}")
	public Response setProjectGroupPermissions(
		@PathParam("projectId") final String projectId,
		@PathParam("groupId") final String groupId,
		@PathParam("relationship") final String relationship
	) {
		try {
			RebacProject what = new RebacProject(projectId, reBACService);
			RebacGroup who = new RebacGroup(groupId, reBACService);
			return setProjectPermissions(what, who, relationship);
		} catch (Exception e) {
			log.error("Error setting project group permission relationships", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path("/{projectId}/permissions/group/{groupId}/{relationship}")
	public Response removeProjectGroupPermissions(
		@PathParam("projectId") final String projectId,
		@PathParam("groupId") final String groupId,
		@PathParam("relationship") final String relationship
	) {
		if (relationship.equals(Schema.Relationship.CREATOR)) {
			return Response.notModified().build();
		}
		try {
			RebacProject what = new RebacProject(projectId, reBACService);
			RebacGroup who = new RebacGroup(groupId, reBACService);
			return removeProjectPermissions(what, who, relationship);
		} catch (Exception e) {
			log.error("Error deleting project group permission relationships", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("/{projectId}/permissions/user/{userId}/{relationship}")
	public Response setProjectUserPermissions(
		@PathParam("projectId") final String projectId,
		@PathParam("userId") final String userId,
		@PathParam("relationship") final String relationship
	) {
		try {
			RebacProject what = new RebacProject(projectId, reBACService);
			RebacUser who = new RebacUser(userId, reBACService);
			return setProjectPermissions(what, who, relationship);
		} catch (Exception e) {
			log.error("Error setting project user permission relationships", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path("/{projectId}/permissions/user/{userId}/{relationship}")
	public Response removeProjectUserPermissions(
		@PathParam("projectId") final String projectId,
		@PathParam("userId") final String userId,
		@PathParam("relationship") final String relationship
	) {
		try {
			RebacProject what = new RebacProject(projectId, reBACService);
			RebacUser who = new RebacUser(userId, reBACService);
			return removeProjectPermissions(what, who, relationship);
		} catch (Exception e) {
			log.error("Error deleting project user permission relationships", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private Response setProjectPermissions(RebacProject what, RebacObject who, String relationship) throws Exception {
		if (new RebacUser(jwt.getSubject(), reBACService).canAdministrate(what)) {
			what.setPermissionRelationships(who, relationship);
			return Response.ok().build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	private Response removeProjectPermissions(RebacProject what, RebacObject who, String relationship) throws Exception {
		if (new RebacUser(jwt.getSubject(), reBACService).canAdministrate(what)) {
			try {
				what.removePermissionRelationships(who, relationship);
				return Response.ok().build();
			} catch (RelationshipAlreadyExistsException e) {
				return Response.notModified().build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createProject(
		final Project project
	) {
		Response res = proxy.createProject(project);
		Id id = res.readEntity(new GenericType<Id>() {});
		String location = res.getHeaderString("Location");
		String server = res.getHeaderString("Server");
		try {
			new RebacUser(jwt.getSubject(), reBACService).createCreatorRelationship(new RebacProject(Integer.toString(id.getId()), reBACService));
		} catch (Exception e) {
			log.error("Error getting user's permissions for project", e);
			// TODO: Rollback potential?
		}
		return Response.status(201).header("Location", location).header("Server", server).entity(id).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(
		@PathParam("id") final String id,
		final Project project
	) {
		try {
			if (new RebacUser(jwt.getSubject(), reBACService).canWrite(new RebacProject(id, reBACService))) {
				return proxy.updateProject(id, project);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			log.error("Error updating project", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteProject(
		@PathParam("id") final String id
	) {
		try {
			if (new RebacUser(jwt.getSubject(), reBACService).canAdministrate(new RebacProject(id, reBACService))) {
				return proxy.deleteProject(id);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			log.error("Error deleting project", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/{project_id}/assets")
	public Response getAssets(
		@PathParam("project_id") final String projectId,
		@QueryParam("types") final List<Assets.AssetType> types
	) {
		try {
			if (new RebacUser(jwt.getSubject(), reBACService).canRead(new RebacProject(projectId, reBACService))) {
				return Response
					.status(Response.Status.OK)
					.entity(proxy.getAssets(projectId, types))
					.type(MediaType.APPLICATION_JSON)
					.build();
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("Error getting project assets", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response createAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final Assets.AssetType type,
		@PathParam("resource_id") final String resourceId
	) {
		try {
			if (new RebacUser(jwt.getSubject(), reBACService).canWrite(new RebacProject(projectId, reBACService))) {
				return proxy.createAsset(projectId, type, resourceId);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			log.error("Error creating project assets", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response deleteAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final Assets.AssetType type,
		@PathParam("resource_id") final String resourceId
	) {
		try {
			if (new RebacUser(jwt.getSubject(), reBACService).canWrite(new RebacProject(projectId, reBACService))) {
				return proxy.deleteAsset(projectId, type, resourceId);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			log.error("Error deleting project assets", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
