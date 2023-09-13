package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.jwt.JsonWebToken;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.Id;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacProject;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

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
					return new RebacUser(jwt.getSubject(), reBACService).canRead(new RebacProject(project.getProjectID()));
				} catch (Exception e) {
					throw new RuntimeException(e);
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
			if (new RebacUser(jwt.getSubject(), reBACService).canRead(new RebacProject(id))) {
				return proxy.getProject(id);
			}
			return Response.status(404).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
			new RebacUser(jwt.getSubject(), reBACService).createCreatorRelationship(new RebacProject(Integer.toString(id.getId())));
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			if (new RebacUser(jwt.getSubject(), reBACService).canWrite(new RebacProject(id))) {
				return proxy.updateProject(id, project);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteProject(
		@PathParam("id") final String id
	) {
		try {
			if (new RebacUser(jwt.getSubject(), reBACService).canAdministrate(new RebacProject(id))) {
				return proxy.deleteProject(id);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Path("/{project_id}/assets")
	public Response getAssets(
		@PathParam("project_id") final String projectId,
		@QueryParam("types") final List<Assets.AssetType> types
	) {
		try {
			if (new RebacUser(jwt.getSubject(), reBACService).canRead(new RebacProject(projectId))) {
				return Response
					.status(Response.Status.OK)
					.entity(proxy.getAssets(projectId, types))
					.type(MediaType.APPLICATION_JSON)
					.build();
			}
			return Response.status(404).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			if (new RebacUser(jwt.getSubject(), reBACService).canWrite(new RebacProject(projectId))) {
				return proxy.createAsset(projectId, type, resourceId);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			if (new RebacUser(jwt.getSubject(), reBACService).canWrite(new RebacProject(projectId))) {
				return proxy.deleteAsset(projectId, type, resourceId);
			}
			return Response.notModified().build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
